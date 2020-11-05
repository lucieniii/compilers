m = [[1, -1, -1, -1, 1, 1],
     [1, 1, -1, -1, 1, 1],
     [1, 1, None, None, 1, 1],
     [-1, -1, -1, -1, 0, None],
     [1, 1, None, None, 1, 1],
     [-1, -1, -1, -1, None, 0]]
dic = {"+": 0, "*": 1, "i": 2, "(": 3, ")": 4, "#": 5}
priority = {"+": 1, "*": 2, "i": 4, "(": 3, ")": 3, "#": 0}

stack = None
buffer = None
i = 0

s = "#"


def compare(in_stack, out_stack):
    return m[dic[in_stack]][dic[out_stack]]


def getchar():
    global i
    try:
        c = buffer[i]
        i += 1
        return c
    except:
        return None


def peek_char():
    global i
    try:
        c = buffer[i]
        return c
    except:
        return None


def error(msg):
    print(msg)
    exit(0)


def success(msg):
    print(msg)


def push_stack_msg(c):
    print("I" + c)


def specification_left():
    global s
    s += "#"
    length = len(s)
    j = 0
    k = 1
    # while k < length:


if __name__ == "__main__":
    print(input())
