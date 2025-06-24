import time
x = 1
def foo():
    x = 2
    def bar():
        global x
        print('Hello World!')
        time_val = time.time()
        print(time_val.real)
    bar()
x = 3
foo()