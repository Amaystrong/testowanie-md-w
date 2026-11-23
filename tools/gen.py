import math, json, sys
D=math.degrees
DEF={'rightArm':(-5,2,0),'leftArm':(5,2,0),'head':(0,0,0),'body':(0,0,0),'rightLeg':(-1.9,12,0),'leftLeg':(1.9,12,0)}
def aim(part,pivot,hand):
    d=[hand[i]-pivot[i] for i in range(3)]; n=math.sqrt(sum(c*c for c in d)); d=[c/n for c in d]
    a=math.asin(max(-1,min(1,d[2])))
    xr=-math.pi-a
    cx=math.cos(xr)
    zr=math.atan2(-d[0]/cx, d[1]/cx)
    dp=DEF[part]
    return {'pos':[round(pivot[i]-dp[i],3) for i in range(3)],'rot':[round(D(xr),2),0,round(D(zr),2)]}
def pose(root=(0,0,0),rootpos=(0,0,0),head=(0,0,0),body=(0,0,0),ra=None,la=None,rl=(0,0,0),ll=(0,0,0),rlp=(0,0,0),llp=(0,0,0)):
    return {'root':{'rot':list(root),'pos':list(rootpos)},'head':{'rot':list(head),'pos':[0,0,0]},'body':{'rot':list(body),'pos':[0,0,0]},
            'rightArm':aim('rightArm',*ra),'leftArm':aim('leftArm',*la),'rightLeg':{'rot':list(rl),'pos':list(rlp)},'leftLeg':{'rot':list(ll),'pos':list(llp)}}
# hands under left cheek
CHEEK_RA=((-3.4,10.0,-3.0),(2.6,-0.4,-4.8)); CHEEK_LA=((4.8,9.0,-2.6),(3.8,-0.8,-4.6))
def cheek(s): return pose(root=(0,0,3*s),head=(6,8+3*s,-10-4*s),body=(0,3*s,0),ra=CHEEK_RA,la=CHEEK_LA,rl=(0,4,1),ll=(0,-4,-1))
HUG_RA=((-3.0,8.5,-2.6),(4.2,1.2,-3.2)); HUG_LA=((3.4,9.0,-4.4),(-3.8,1.6,-5.2))
def hug(s): return pose(root=(4,-18+4*s,2*s),rootpos=(0,-1.0,0),head=(14,-6,12+5*s),body=(8,0,0),ra=HUG_RA,la=HUG_LA,rl=(-14,6,2),ll=(-4,-6,-2))
def lean(s): return pose(root=(18,-12+6*s,6*s),rootpos=(0,-0.6,0),head=(-22,-4*s,8*s),body=(-4,0,0),ra=HUG_RA,la=HUG_LA,rl=(10,6,2),ll=(14,-6,-2))
LOW_RA=((-4.2,5.0,-3.0),(5.0,6.0,-4.4)); LOW_LA=((4.4,8.6,-4.8),(-5.0,8.0,-5.6))
def low(): return pose(root=(2,-6,0),rootpos=(0,-0.4,0),head=(8,6,-6),body=(4,0,0),ra=LOW_RA,la=LOW_LA,rl=(-6,4,1),ll=(-2,-4,-1))
kf=[(0.0,cheek(1)),(0.8,cheek(-1)),(1.6,cheek(1)),(2.4,cheek(-1)),(3.1,hug(1)),(3.9,hug(-1)),(4.7,hug(1)),
    (5.5,lean(1)),(6.3,lean(-1)),(7.1,lean(1)),(7.9,lean(-1)),(8.7,lean(1)),(9.5,low()),(10.5,cheek(-1))]
out={'length':11.5,'rootPivotY':12,'keyframes':[{'t':t,**p} for t,p in kf]}
json.dump(out,open(sys.argv[1],'w'),indent=1)
