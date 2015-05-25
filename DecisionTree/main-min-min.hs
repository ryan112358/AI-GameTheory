import Data.Ord
import Data.List
import Data.List.Split
import Control.Arrow
import Data.Tree
import qualified Data.Vector as V
import Data.Maybe
import System.Random
import System.Environment

-------------------
-- Decision Tree --
-------------------

type Example = V.Vector Int
type Class = Int

data Feature = Feature Int Int deriving (Read,Show,Eq,Ord)

data DTree = Leaf Class | DNode Feature DTree DTree
	deriving (Read,Show)

---------------------
-- Pretty Printing --
---------------------

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

size :: DTree -> Int
size (Leaf _) = 1
size (DNode _ l r) = 1 + size l + size r

satisfies :: Feature -> Example -> Bool
satisfies (Feature i v) ex = ex V.! i <= v

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
	
test :: DTree -> [(Example,Class)] -> Double
test tree exs = suc / len
	where 
		(xs,cs) = unzip exs
		cs' = classifyAll tree xs
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
parse = sortBy (comparing snd) . map (V.fromList.init &&& last) . readCSV
	
main = 	do
	args <- getArgs
	let opts = zip args (tail args)
	let load = fromMaybe (return []) . fmap getExamples . flip lookup opts
	training <- load "-train"
	testing <- load "-test" 
	let feats = features $ map fst training
	let tree = flip prune pruning $ train training feats
	let verbose = elem "-v" args
	putStrLn $ "Test Set Size: " ++ show (length training)
	putStrLn $ "Training Rate: " ++ show (test tree training)
	putStrLn $ "Testing Rate: " ++ show (test tree testing)
	putStrLn $ "Tree Size: " ++ show (size tree)
	if verbose then putStrLn (drawDTree tree) else return ()