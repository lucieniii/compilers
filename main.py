import sys

char = ''
token = ''
num = ''
symbol = ''
buffer = ''
i = 0
length = 0

blank_char_set = ['\n', '\r', '\t', ' ']
reserve_set = {'BEGIN': 'begin', 'END': 'End', 'FOR': 'For', 'IF': 'If', 'THEN': 'Then', 'ELSE': 'Else'}
stop_sgn = 'STOP'


def is_not_stop():
    if char is stop_sgn:
        return False
    else:
        return True


def getchar():
    global char, i
    if i > length:
        char = stop_sgn
        return
    char = buffer[i]
    i += 1


def clear_token():
    global token
    token = ''


def is_blank():
    global char
    if char in blank_char_set:
        return True
    else:
        return False


def is_alpha():
    return char.isalpha()


def is_digit():
    return char.isdigit()


def is_letter():
    return is_alpha() | is_digit()


def is_colon():
    if char is ':':
        return True
    else:
        return False


def is_comma():
    if char is ',':
        return True
    else:
        return False


def is_semi():
    if char is ';':
        return True
    else:
        return False


def is_equ():
    if char is '=':
        return True
    else:
        return False


def is_plus():
    if char is '+':
        return True
    else:
        return False


def is_minus():
    if char is '-':
        return True
    else:
        return False


def is_divide():
    if char is '/':
        return True
    else:
        return False


def is_star():
    if char is '*':
        return True
    else:
        return False


def is_l_par():
    if char is '(':
        return True
    else:
        return False


def is_r_par():
    if char is ')':
        return True
    else:
        return False


def cat_token():
    global token
    token += char


def retract():
    global i
    i -= 1


def reserver():
    return reserve_set.get(token)


def trans_num():
    return int(token)


def error():
    print('Unknown')
    exit(0)


def get_sym():
    global symbol, num

    clear_token()
    getchar()
    while is_blank() and is_not_stop():
        getchar()
    if not is_not_stop():
        exit(0)
    if is_alpha():
        while is_letter() and is_not_stop():
            cat_token()
            getchar()
        retract()
        if reserver():
            symbol = reserver()
        else:
            symbol = 'Ident(' + token + ')'
    elif is_digit():
        while is_digit() and is_not_stop():
            cat_token()
            getchar()
        retract()
        num = trans_num()
        symbol = num
    elif is_colon():
        getchar()
        if is_equ():
            symbol = 'Assign'
        else:
            retract()
            symbol = 'Colon'
    elif is_plus():
        symbol = 'Plus'
    elif is_star():
        symbol = 'Star'
    elif is_l_par():
        symbol = 'LParenthesis'
    elif is_r_par():
        symbol = 'RParenthesis'
    elif is_comma():
        symbol = 'Comma'
    else:
        error()
    print(symbol)


if __name__ == '__main__':
    buffer = sys.stdin.read()
    length = len(buffer)
    while True:
        get_sym()

