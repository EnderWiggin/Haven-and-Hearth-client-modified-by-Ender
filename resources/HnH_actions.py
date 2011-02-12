from struct import pack

def generate(parent, v, name, req, hk, *params):
    tmp = str(parent) + "\x00"
    tmp += pack("H", v)
    tmp += str(name) + "\x00"
    tmp += str(req) + "\x00"
    tmp += str(hk) + "\x00"
    tmp += pack("H",len(params))
    for param in params:
        tmp += str(param) + "\x00"
    return "action\x00" +pack("L",len(tmp))+tmp
