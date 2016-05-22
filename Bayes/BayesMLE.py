from scipy.optimize import minimize
import numpy as np
import pdb
import warnings
import argparse
from scipy.stats.mstats import gmean
from multiprocessing import Pool

warnings.filterwarnings('error')
# High level info:
# input features [A,B,C]
# output feature D
# Want P(D=d | A=a, B=b, C=c)
# Need P(A=a | D=d), P(B=b | D=d),P(C=c | D=d), P(D=d) for all a,b,c,d
# #D*(1+#A+#B+#C) parameters

parser = argparse.ArgumentParser()
parser.add_argument('--data')
parser.add_argument('--trials', type=int, default=1)
parser.add_argument('--train', type=int, default=100)
parser.add_argument('--test', type=int, default=100)
parser.add_argument('--inputs', nargs='+', type=int, default=[2,2,2])
parser.add_argument('--output', type=int, default=2)
parser.add_argument('--verbose', action='store_true')
parser.add_argument('--parallel', action='store_true')

args = parser.parse_args()

def norm(arr): 
    return arr.astype(float) / arr.sum()

# Consumes 4 integers, representing the number of values each feature can take on
# Returns the cumulaitve distribution functions for all probabilities/conditional probabilities of interest
def random_params(inputs, output):
    B = norm(np.random.rand(output) + 0.2) # P(D = d)
    def foo(vals):
        R = np.random.rand(output, vals)
        return R / R.sum(axis=1)[:, np.newaxis]
    A = [foo(i) for i in inputs]
    return A, B

# consumes conditional probability table
# probabilities for each class
# and number of rows to generate
# returns a dataset (X, Y)
def gen_data(A,B,N):
    BB = B.cumsum() # P(D <= d)
    AA = [A2.cumsum(axis=1) for A2 in A]

    Y = np.searchsorted(BB, np.random.rand(N))
    X = np.zeros((N, len(A)), dtype=int)
    for i in range(N):
        y = Y[i]
        for k in range(len(A)):
            X[i, k] = np.searchsorted(AA[k][y], np.random.rand())
    return X,Y

def learn_naive(inputs, output, X, Y):
    B = np.array([np.mean(Y==y) for y in range(output)])
    def foo(y, i):
        return np.array([np.mean(X[Y==y, i] == x) for x in range(inputs[i])])
    A = [[foo(y, i) for y in range(output)] for i in range(len(inputs))]
    return A, B

def reconstruct(inputs, output, params):
    B = params[0:output]
    A = []
    curr = output
    for i in range(len(inputs)):
        size = inputs[i] * output
        A.append(np.reshape(params[curr:curr+size], (output, inputs[i])))
        curr += size 
    return A,B

def deconstruct(A,B):
    flat = map(lambda a: np.array(a).flatten(), A)
    return np.concatenate([B,np.concatenate(flat)])

def loglike(inputs, output, A, B, X, Y):
    probs = np.zeros(Y.shape)
    for i in range(len(Y)):
        foo = lambda y: B[y] * np.prod([A[j][y][X[i,j]] for j in range(len(inputs))])
        probs[i] = norm(np.array([foo(y) for y in range(output)]))[Y[i]]
        if probs[i] == 0: return np.inf
    return -np.log(probs).sum()

def learn_mle(inputs, output, X,Y):
    def loglike2(params):
        A, B = reconstruct(inputs, output, params)
        return loglike(inputs, output, A, B, X, Y)

    x0 = deconstruct(*learn_naive(inputs, output,X,Y))
#    x0 = deconstruct(*uniform(inputs, output))
    #x0 = np.maximum(np.minimum(x0, 0.999), 0.001)
    N = len(x0)
    def jac(i, j):
        ans = np.zeros(N)
        ans[i:j] = 1
        return lambda _: ans
    idx = np.cumsum([1] + inputs)
    d = output
    cons = [{ 'type':'eq', 'fun': lambda p: p[0:d].sum()-1, 'jac' : jac(0, d)}]
    for j in range(len(idx)-1):
        for i in range(idx[j], idx[j+1]):
            fun = lambda p: p[i*d : (i+1)*d].sum()-1
            cons.append({'type':'eq','fun':fun,'jac':jac(i*d, (i+1)*d)})

    opts = { 'disp': True } if args.verbose else {}
    params = minimize(loglike2, x0, method='SLSQP', bounds = [(0.001, 0.999)]*N, constraints=cons, options=opts)
    return reconstruct(inputs, output, params.x)

def compare(dummy):
    np.random.seed(dummy)
    A, B = random_params(args.inputs, args.output) 
    trainX, trainY = gen_data(A, B, args.train)
    testX, testY = gen_data(A, B, args.test)
    A1, B1 = learn_naive(args.inputs, args.output, trainX, trainY)
    A2, B2 = learn_mle(args.inputs, args.output, trainX, trainY)
    p1 = loglike(args.inputs, args.output, A1, B1, testX, testY)
    p2 = loglike(args.inputs, args.output, A2, B2, testX, testY)
    q1 = loglike(args.inputs, args.output, A1, B1, trainX, trainY)
    q2 = loglike(args.inputs, args.output, A2, B2, trainX, trainY)
    print np.exp(q1-q2), np.exp(p1-p2)
    
    #c1 = eval_classification(args.inputs, args.output, A1, B1, testX, testY)
    #c2 = eval_classification(args.inputs, args.output, A2, B2, testX, testY)
    #print c1, c2
    return np.exp(p1-p2)

if __name__ == '__main__':
    if args.parallel:
        pool = Pool()
        ans = pool.map(compare, range(args.trials))
    else:
        ans = map(compare, range(args.trials))
    print 'Geometric Mean:', gmean(ans)
