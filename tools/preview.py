import json, math, sys, numpy as np
from PIL import Image, ImageDraw
d=json.load(open(sys.argv[1])); L=d['length']; K=d['keyframes']
DEF={'rightArm':(-5,2,0),'leftArm':(5,2,0),'head':(0,0,0),'body':(0,0,0),'rightLeg':(-1.9,12,0),'leftLeg':(1.9,12,0)}
BOX={'head':((-4,-8,-4),(8,8,8)),'body':((-4,0,-2),(8,12,4)),'rightArm':((-3,-2,-2),(4,12,4)),'leftArm':((-1,-2,-2),(4,12,4)),'rightLeg':((-2,0,-2),(4,12,4)),'leftLeg':((-2,0,-2),(4,12,4))}
COL={'head':(200,110,50),'body':(40,140,50),'rightArm':(235,190,150),'leftArm':(215,170,130),'rightLeg':(40,40,60),'leftLeg':(60,60,80)}
def ease(x): return 0.5-0.5*math.cos(math.pi*x)
def sample(t):
    t%=L
    for i in range(len(K)):
        a=K[i]; b=K[(i+1)%len(K)]; tb=b['t'] if i+1<len(K) else L
        if a['t']<=t<tb or i==len(K)-1:
            f=ease((t-a['t'])/(tb-a['t'])); break
    def lerp(x,y): return [x[j]+(y[j]-x[j])*f for j in range(3)]
    return {p:{'rot':lerp(a[p]['rot'],b[p]['rot']),'pos':lerp(a[p]['pos'],b[p]['pos'])} for p in ['root','head','body','rightArm','leftArm','rightLeg','leftLeg']}
def Rx(a): c,s=math.cos(a),math.sin(a); return np.array([[1,0,0,0],[0,c,-s,0],[0,s,c,0],[0,0,0,1]])
def Ry(a): c,s=math.cos(a),math.sin(a); return np.array([[c,0,s,0],[0,1,0,0],[-s,0,c,0],[0,0,0,1]])
def Rz(a): c,s=math.cos(a),math.sin(a); return np.array([[c,-s,0,0],[s,c,0,0],[0,0,1,0],[0,0,0,1]])
def T(x,y,z): m=np.eye(4); m[:3,3]=[x,y,z]; return m
def S(x,y,z): return np.diag([x,y,z,1.0])
r=math.radians
def render(t,camyaw=-20,size=160):
    P=sample(t); rt=P['root']; pv=d['rootPivotY']/16
    root=T(*[v/16 for v in rt['pos']])@T(0,pv,0)@Rz(r(rt['rot'][2]))@Ry(r(rt['rot'][1]))@Rx(r(rt['rot'][0]))@T(0,-pv,0)
    model=root@S(-1,-1,1)@S(.9375,.9375,.9375)@T(0,-1.501,0)
    cam=Rx(r(10))@Ry(r(camyaw))
    faces=[]
    for p in BOX:
        pp=P[p]; dp=DEF[p]
        M=model@T(*[(dp[i]+pp['pos'][i])/16 for i in range(3)])@Rz(r(pp['rot'][2]))@Ry(r(pp['rot'][1]))@Rx(r(pp['rot'][0]))
        (x0,y0,z0),(w,h,dd)=BOX[p]
        cs=[(x0+w*i,y0+h*j,z0+dd*k) for i in (0,1) for j in (0,1) for k in (0,1)]
        W=[(cam@M@np.array([c[0]/16,c[1]/16,c[2]/16,1]))[:3] for c in cs]
        quads=[(0,1,3,2,'x0'),(4,5,7,6,'x1'),(0,1,5,4,'y0'),(2,3,7,6,'y1'),(0,2,6,4,'z0'),(1,3,7,5,'z1')]
        for q in quads:
            pts=[W[q[i]] for i in range(4)]; depth=np.mean([pt[2] for pt in pts])
            col=COL[p]
            if p=='head' and q[4]=='z0': col=(250,200,170)
            if q[4] in('x0','x1'): col=tuple(int(c*.8) for c in col)
            faces.append((depth,pts,col))
    img=Image.new('RGB',(size,size),(40,32,28)); dr=ImageDraw.Draw(img)
    sc=size/2.6
    for depth,pts,col in sorted(faces,key=lambda f:-f[0]):
        dr.polygon([(size/2-pt[0]*sc, size*0.92-pt[1]*sc) for pt in pts],fill=col,outline=(0,0,0))
    return img
ts=[float(x) for x in sys.argv[3].split(',')]; SZ=int(sys.argv[4]) if len(sys.argv)>4 else 160
cols=min(8,len(ts)); rows=(len(ts)+cols-1)//cols; sheet=Image.new('RGB',(SZ*cols,SZ*rows))
for i,t in enumerate(ts): sheet.paste(render(t,size=SZ),(SZ*(i%cols),SZ*(i//cols)))
sheet.save(sys.argv[2])
