m = [[1, -1, -1, -1, 1, 1],
     [1, 1, -1, -1, 1, 1],
     [1, 1, None, None, 1, 1],
     [-1, -1, -1, -1, 0, None],
     [1, 1, None, None, 1, 1],
     [-1, -1, -1, -1, None, 0]]
dic = {"+": 0, "*": 1, "i": 2, "(": 3, ")": 4, "#": 5}

stack = None
buffer = None
i = 0


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


class Stack:

    stack = ["#"]
    top = 1

    def stack_top_op(self):
        if self.stack[self.top - 1] in dic.keys():
            return self.stack[self.top - 1]
        else:
            return self.stack[self.top - 2]

    def push(self, e):
        try:
            self.stack[self.top] = e
        except:
            self.stack.append(e)
        self.top += 1

    def pop(self):
        try:
            c = self.stack[self.top - 1]
            self.top -= 1
            return c
        except:
            error("RE3")


def analyse():

    while True:
        c = peek_char()
        if c == "i":
            getchar()
            push_stack_msg(c)
            success("R")
            stack.push("E")
            continue
        t = stack.stack_top_op()
        cmp = compare(t, c)
        if cmp == -1:
            getchar()
            push_stack_msg(c)
            stack.push(c)
        elif cmp == 0:
            getchar()
            if c == ")":
                push_stack_msg(")")
                e = stack.pop()
                left = stack.pop()
                if e == "E" and left == "(":
                    success("R")
                    stack.push("E")
                else:
                    error("RE2")
            else:
                if stack.pop() != "E":
                    error("REG")
                exit(0)
        elif cmp == 1:
            e1 = stack.pop()
            op = stack.pop()
            e2 = stack.pop()
            if e1 == e2 == "E" and op in ["*", "+"]:
                success("R")
                stack.push("E")
            else:
                error("RE1")
        else:
            error("E")


if __name__ == "__main__":
    buffer = input() + "#"
    # print(buffer)
    stack = Stack()
    analyse()
