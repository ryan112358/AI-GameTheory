{-# LANGUAGE OverloadedStrings #-}
import Data.Ord
import Data.List
import Data.List.Split
import Control.Arrow
import Data.Tree
import qualified Data.Vector as V
import Data.Maybe
import System.Random
import System.Environment
import Control.Parallel.Strategies
import Control.DeepSeq
import Text.LaTeX.Base
import Text.LaTeX.Base.Render
import Text.LaTeX.Packages.AMSMath
import qualified Text.LaTeX.Packages.Trees as T
import qualified Text.LaTeX.Packages.Trees.Qtree as Q

-------------------
-- Decision Tree --
-------------------

type Example = V.Vector Int
type Class = Int

data Feature = Feature Int Int deriving (Read,Show,Eq,Ord)

data DTree = Leaf Class | DNode Feature DTree DTree
	deriving (Read,Show)
	
instance NFData Feature where
	rnf (Feature i v) = rnf i `seq` rnf v `seq` ()

instance NFData DTree where
	rnf (Leaf c) = rnf c
	rnf (DNode f left right) = rnf f `seq` rnf left `seq` rnf right `seq` ()

---------------------
-- Pretty Printing --
---------------------

latexDTree :: DTree -> LaTeX
latexDTree = Q.tree id . convert 
	where
		showF (Feature i v) = math $ "v" !: rendertex i <=: rendertex v
		convert :: DTree -> T.Tree LaTeX
		convert (Leaf c) = T.Leaf (rendertex c)
		convert (DNode f l r) = T.Node (Just $ showF f) [convert l,convert r]

drawDTree :: DTree -> String 
drawDTree = drawTree . convert where
	convert (Leaf c) = Node (show c) []
	convert (DNode f l r) = Node (showF f) [convert l, convert r]
	showF (Feature i v) = "v_" ++ show i ++ " <= " ++ show v

------------------------
-- Information Theory --
------------------------

entropy :: [(Example,Class)] -> Double
entropy ex = sum $ map f vs
	where
		len = genericLength ex
		vs = map (\xs -> genericLength xs  / len) . group . map snd $ ex
		f 0 = 0; f p = -p*logBase 2 p 

bestFeature :: [(Example,Class)] -> [Feature] -> Feature
bestFeature exs = minimumBy (comparing info)
	where 
		g x = genericLength x * entropy x
		h (a,b) = (g a + g b) / genericLength exs
		info f = h $ splitF f exs

----------------------
-- Simple Functions --
---------------------- 		

depth :: DTree -> Int
depth (Leaf _) = 1
depth (DNode _ l r) = 1 + max (depth l) (depth r)

size :: DTree -> Int
size (Leaf _) = 1
size (DNode _ l r) = 1 + size l + size r

satisfies :: Feature -> Example -> Bool
satisfies (Feature i v) ex = ex V.! i <= v

splitF :: Feature -> [(Example,Class)] -> ([(Example,Class)],[(Example,Class)])
splitF f = partition (satisfies f . fst)

rmLeft,rmRight :: Feature -> [Feature] -> [Feature]
rmLeft (Feature i v) = filter (\(Feature j u) -> i/=j || u < v)
rmRight (Feature i v) = filter (\(Feature j u) -> i/=j || u > v)

mostCommon, mostCommon' :: Ord a => [a] -> a
mostCommon' = head . maximumBy (comparing length) . group
mostCommon = mostCommon' . sort
		
features :: [Example] -> [Feature] 
features = nub . sort . concatMap (zipWith Feature [0..] . V.toList) 
		
classify :: DTree -> Example -> Class
classify (Leaf c) _ = c 
classify (DNode f l r) ex
	| satisfies f ex = classify l ex
	| otherwise = classify r ex
	
classifyAll :: DTree -> [Example] -> [Class]
classifyAll tree = map (classify tree)

bagClassify :: [DTree] -> [Example] -> [Class]
bagClassify trees exs = map mostCommon $ transpose classes 
	where classes = map (flip classifyAll exs) trees
	
------------------------------------
-- Training, Pruning, and Testing --
------------------------------------

train :: [(Example,Class)] -> [Feature] -> DTree
train exs fs 
	| entropy exs == 0 || null fs = Leaf best 
	| otherwise = DNode f (train ls fl) (train rs fr)
	where
		best = mostCommon' $ map snd exs 
		f = bestFeature exs fs
		(ls,rs) = splitF f exs
		(fl, fr) = (rmLeft f fs, rmRight f fs)
		
prune :: DTree -> [(Example,Class)] -> DTree
prune (Leaf c) _ = Leaf c
prune tree@(DNode f l r) exs
	| leafErr < nodeErr = Leaf best
	| otherwise = DNode f (prune l ls) (prune r rs)
	where 
		(ls,rs) = splitF f exs
		best = mostCommon' $ map snd exs 
		nodeErr = 1 - test tree exs
		leafErr = 1 - genericLength (filter ((==best).snd) exs) / genericLength exs
	
test :: DTree -> [(Example,Class)] -> Double
test tree exs = suc / len
	where 
		(xs,cs) = unzip exs
		cs' = classifyAll tree xs
		len = genericLength exs
		suc = genericLength . filter (uncurry (==)) $ zip cs cs'

-----------------------
-- Ensemble Learning --
-----------------------		
		
bagTrain :: Int -> [(Example,Class)] -> [Feature] -> [DTree]
bagTrain bags exs fs = parMap rdeepseq (flip train fs . map (axs V.!)) . take bags $ chunksOf bag rs
	where
		bag = n `div` 2
		n = length exs - 1
		axs = V.fromList exs 
		rs = randomRs (0,n) (mkStdGen 0)
		
bagTest :: [DTree] -> [(Example,Class)] -> Double
bagTest trees exs = suc / len
	where
		(xs,cs) = unzip exs
		cs' = bagClassify trees xs
		len = genericLength exs
		suc = genericLength . filter (uncurry (==)) $ zip cs cs'

-------------------------
-- Interface + Parsing --
-------------------------

getExamples :: FilePath -> IO [(Example,Class)]
getExamples = fmap parse . readFile
	
readCSV :: String -> [[Int]]
readCSV = map parseLine . lines . fixFormat where
	fixFormat = map (\c -> if c=='\r' then '\n' else c)
	parseLine = map read . splitOn ","

parse :: String -> [(Example,Class)]
parse = sortBy (comparing snd) . map (toExample.init &&& last) . readCSV
	
toExample = V.fromList
	
main = 	do
	args <- getArgs
	let opts = zip args (tail args)
	let load = fromMaybe (return []) . fmap getExamples . flip lookup opts
	training <- load "-train"
	pruning <- load "-prune" 
	testing <- load "-test" 
	learning <- fromMaybe (return []) . fmap (fmap (map toExample.readCSV).readFile) $ lookup "-learn" opts
	let bag = fromMaybe 0 . fmap read $ lookup "-bag" opts
	let feats = features $ map fst training
	let tree = flip prune pruning $ train training feats
	let trees = map (flip prune pruning) $ bagTrain bag training feats
	let verbose = elem "-v" args
	let save = lookup "-save" opts
	putStrLn $ "Test Set Size: " ++ show (length training)
	if bag == 0 then do
		putStrLn $ "Training Rate: " ++ show (test tree training)
		putStrLn $ "Testing Rate: " ++ show (test tree testing)
		putStrLn $ "Tree Size: " ++ show (size tree)
		if null learning then return () else putStrLn $ "Classifications: " ++ show (classifyAll tree learning)
		if verbose then putStrLn (drawDTree tree) else return ()
		if isJust save then writeFile (fromJust save) (show tree) else return ()
	else do
		putStrLn $ "Training Rate: " ++ show (bagTest trees training)
		putStrLn $ "Testing Rate: " ++ show (bagTest trees testing)
		putStrLn $ "Tree Sizes: " ++ show (map size trees) 
		if null learning then return () else putStrLn $ "Classifications: " ++ show (bagClassify trees learning)
		if verbose then putStrLn (drawDTree $ trees!!0) else return ()
		if isJust save then writeFile (fromJust save) (show trees) else return ()
