from struct import pack

def generate(name, path):
    tmp = str(name) + "\x00"
    f = open(path,"rb")
    tmp += f.read()
    f.close();
    return "code\x00" +pack("L",len(tmp))+tmp
