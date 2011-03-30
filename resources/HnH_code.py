from struct import pack

def generate(name, path):
    tmp = name + b"\x00"
    f = open(path,"rb")
    tmp += f.read()
    f.close();
    return b"code\x00" +pack("L",len(tmp))+tmp
