from struct import pack

def generate(parent, v, name, req, hk, *params):
    tmp = parent + b"\x00"
    tmp += pack("H", v)
    tmp += name + b"\x00"
    tmp += req + b"\x00"
    tmp += hk + b"\x00"
    tmp += pack("H",len(params))
    for param in params:
        tmp += param + b"\x00"
    return b"action\x00" +pack("L",len(tmp))+tmp
