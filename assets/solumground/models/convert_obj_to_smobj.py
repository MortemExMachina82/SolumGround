import sys
import os

inpath = sys.argv[1]
if len(sys.argv) == 2:
    outpath = inpath[0:len(inpath)-3]+"smobj"
else:
    outpath = sys.argv[2]


def load_OBJ(filename):
    #try to open the file if unable to create the file
    try:
        with open(filename, "r") as f:
            data = f.readlines()
    except:
        print("file not found")

    has_vtcords = False
    has_vtnormal = False
    verts = []
    lines = []
    vtcords = []
    vtnormals = []
    trifaces = []
    quadfaces = []

    #go thru each line of data
    for line in data:
        elem = line.split()
        if elem:
            #if vertex add to verts
            if elem[0] == "v":
                verts.append([float(elem[1]), float(elem[2]), float(elem[3])])
            #if line add to lines
            elif elem[0] == "l":
                lines.append([int(elem[1]), int(elem[2])])
            #if texture cordinant add to vtcords
            elif elem[0] == "vt":
                #note use 1-float(elem[2])
                vtcords.append([float(elem[1]), 1-float(elem[2])])
                has_vtcords = True
            #if vertex normal add to vtnormals
            elif elem[0] == "vn":
                vtnormals.append([float(elem[1]), float(elem[2]), float(elem[3])])
                has_vtnormal = True
            #if face add to faces
            elif elem[0] == "f" and len(elem) == 4:
                #use difrent extraction methodes depending on cercomestances
                if has_vtcords == False and has_vtnormal == False:
                    trifaces.append([int(elem[1])-1, int(elem[2])-1, int(elem[3])-1])
                if has_vtcords == True and has_vtnormal == False:
                    nelem1 = elem[1].split('/')
                    nelem2 = elem[2].split('/')
                    nelem3 = elem[3].split('/')
                    trifaces.append([[int(nelem1[0])-1, int(nelem1[1])-1], [int(nelem2[0])-1, int(nelem2[1])-1], [int(nelem3[0])-1, int(nelem3[1])-1]])
                if has_vtcords == True and has_vtnormal == True:
                    nelem1 = elem[1].split("/")
                    nelem2 = elem[2].split("/")
                    nelem3 = elem[3].split("/")
                    trifaces.append([[int(nelem1[0])-1, int(nelem1[1])-1, int(nelem1[2])-1], [int(nelem2[0])-1, int(nelem2[1])-1, int(nelem2[2])-1], [int(nelem3[0])-1, int(nelem3[1])-1, int(nelem3[2])-1]])

                if has_vtcords == False and has_vtnormal == True:
                    nelem1 = elem[1].split("//")
                    nelem2 = elem[2].split("//")
                    nelem3 = elem[3].split("//")
                    trifaces.append([[int(nelem1[0])-1, int(nelem1[1])-1], [int(nelem2[0])-1, int(nelem2[1])-1], [int(nelem3[0])-1, int(nelem3[1])-1]])
            elif elem[0] == "f" and len(elem) == 5:
                if has_vtcords == False and has_vtnormal == False:
                    quadfaces.append([int(elem[1])-1, int(elem[2])-1, int(elem[3])-1, int(elem[4])-1])
                if has_vtcords == True and has_vtnormal == False:
                    nelem1 = elem[1].split('/')
                    nelem2 = elem[2].split('/')
                    nelem3 = elem[3].split('/')
                    nelem4 = elem[4].split('/')
                    quadfaces.append([[int(nelem1[0])-1, int(nelem1[1])-1], [int(nelem2[0])-1, int(nelem2[1])-1], [int(nelem3[0])-1, int(nelem3[1])-1], [int(nelem4[0])-1, int(nelem4[1])-1]])

    return verts, lines, vtcords, vtnormals, trifaces, quadfaces, has_vtcords, has_vtnormal

if os.path.isdir(inpath):
    temp = os.listdir(inpath)
    infiles_list = []
    for X in temp:
        if X[len(X)-4:] == ".obj":
            infiles_list.append(os.path.join(inpath,X))
    
    outfiles_list = []
    for X in infiles_list:
        X = os.path.basename(X)
        outfiles_list.append(os.path.join(outpath, X[:len(X)-4]+".smobj"))
else:
    infiles_list = [inpath]
    outfiles_list = [outpath]




for fileindex in range(0,len(infiles_list)):

    verts,temp,vtcords,temp, trifaces,quadfaces, has_vetexcords, has_vertexnormals = load_OBJ(infiles_list[fileindex])
    

    maxX = 1
    maxY = 1
    maxZ = 1

    for V in verts:
        if V[0] > maxX:maxX = int(V[0])+1
        if V[1] > maxY:maxY = int(V[1])+1
        if V[2] > maxZ:maxZ = int(V[2])+1

        if -V[0] > maxX:maxX = -int(V[0])+1
        if -V[1] > maxY:maxY = -int(V[1])+1
        if -V[2] > maxZ:maxZ = -int(V[2])+1

    outVerts = b''

    byo = "little"

    for V in verts:
        X = int((V[0]*32767)/maxX)
        Y = int((V[1]*32767)/maxY)
        Z = int((V[2]*32767)/maxZ)

        bX = X.to_bytes(2, byo, signed=True)
        bY = Y.to_bytes(2, byo, signed=True)
        bZ = Z.to_bytes(2, byo, signed=True)

        
        outVerts += bX+bY+bZ

    outvtcords = b''
    
    for Vt in vtcords:
        U = int(Vt[0]*65535)
        V = int((Vt[1])*65535)
        
        bU = U.to_bytes(2, byo, signed=False)
        bV = V.to_bytes(2, byo, signed=False)
       
        
        outvtcords += bU+bV

    outtriFaces = b''
    if(len(verts) < 255 and len(vtcords) < 255):
        facesize = 1
    elif(len(verts) < 65535 and len(vtcords) < 65535):
        facesize = 2
    elif(len(verts) < 16777215 and len(vtcords) < 16777215):
        facesize = 3
    for F in trifaces:
        if has_vetexcords:
            V1 = F[0][0].to_bytes(facesize, byo, signed=False)
            V2 = F[1][0].to_bytes(facesize, byo, signed=False)
            V3 = F[2][0].to_bytes(facesize, byo, signed=False)
            V1c = F[0][1].to_bytes(facesize, byo, signed=False)
            V2c = F[1][1].to_bytes(facesize, byo, signed=False)
            V3c = F[2][1].to_bytes(facesize, byo, signed=False)
            outtriFaces += V1+V1c + V2+V2c + V3+V3c
        else:
            V1 = F[0].to_bytes(facesize, byo, signed=False)
            V2 = F[1].to_bytes(facesize, byo, signed=False)
            V3 = F[2].to_bytes(facesize, byo, signed=False)
            outtriFaces += V1 + V2 + V3
    outquadFaces = b''
    for F in quadfaces:
        if has_vetexcords:
            V1 = F[0][0].to_bytes(facesize, byo, signed=False)
            V2 = F[1][0].to_bytes(facesize, byo, signed=False)
            V3 = F[2][0].to_bytes(facesize, byo, signed=False)
            V4 = F[3][0].to_bytes(facesize, byo, signed=False)
            V1c = F[0][1].to_bytes(facesize, byo, signed=False)
            V2c = F[1][1].to_bytes(facesize, byo, signed=False)
            V3c = F[2][1].to_bytes(facesize, byo, signed=False)
            V4c = F[3][1].to_bytes(facesize, byo, signed=False)
            outquadFaces += V1+V1c + V2+V2c + V3+V3c + V4+V4c
        else:
            V1 = F[0].to_bytes(facesize, byo, signed=False)
            V2 = F[1].to_bytes(facesize, byo, signed=False)
            V3 = F[2].to_bytes(facesize, byo, signed=False)
            outquadFaces += V1 + V2 + V3
        

    with open(outfiles_list[fileindex], "wb") as f:
        f.write(b'smobj{version="1.0"}\n')
        f.write(b'note{"Made By Me"}\n')
        f.write(b'attributes{sizex="'+maxX.to_bytes(4, byo, signed=False)+b'" sizey="'+maxY.to_bytes(4, byo, signed=False)+b'" sizez="'+maxZ.to_bytes(4, byo, signed=False)+b'" ')
        f.write(b'nverts="'+len(verts).to_bytes(4, byo, signed=False)+b'" textured="')
        if len(vtcords) == 0:
            f.write(b'f" ')
        else:
            f.write(b't" ')
        f.write(b'ntexturecords="'+len(vtcords).to_bytes(4, byo, signed=False)+b'" ntrianglefaces="'+len(trifaces).to_bytes(4, byo, signed=False)+b'" nquadfaces="')
        f.write(int(len(quadfaces)).to_bytes(4, byo, signed=False)+b'"}\n')
        
        
        f.write(b'VERTS{')
        
        f.write(outVerts)
        f.write(b'}\nTEXTURECORDS{')
        f.write(outvtcords)
        if len(trifaces) != 0:
            f.write(b'}\nTRIANGLEFACES')
            f.write(bytes(str(facesize), "utf-8")+b'{')
            f.write(outtriFaces)
            f.write(b'}\n')
        if len(quadfaces) != 0:
            f.write(b'}\nQUADFACES')
            f.write(bytes(str(facesize), "utf-8")+b'{')
            f.write(outquadFaces)
            f.write(b'}\n')
        f.write(b'END')


    print("model#",fileindex,len(verts),len(trifaces))





