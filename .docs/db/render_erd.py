"""Landscape ERD of core features (MySQL Workbench style) from .docs/db/schema.json.
Run: python .docs/db/render_erd.py"""
import json
from PIL import Image, ImageDraw, ImageFont
S=2
D=json.load(open('.docs/db/schema.json',encoding='utf8'));T=D['tabs']
F=lambda n,b=False:ImageFont.truetype('C:/Windows/Fonts/'+('segoeuib.ttf' if b else 'segoeui.ttf'),n*S)
fN,fH,fT,fL=F(13),F(15,True),F(11),F(13,True)
W,RH,HH,FH=330,24,32,22
LAY=[  # columns left -> right
 ['users','topics'],
 ['flashcard_decks','writing_prompts','speaking_lessons','listening_lessons','reading_lessons','exams'],
 ['flashcards','writing_submissions','speaking_attempts','questions','practice_attempts'],
 ['user_flashcard_progress','ai_feedbacks','speaking_prompt_results','question_options','practice_attempt_answers'],
]
ALIGN={'flashcards':'flashcard_decks','writing_submissions':'writing_prompts','speaking_attempts':'speaking_lessons','questions':'reading_lessons','practice_attempts':'exams',
 'user_flashcard_progress':'flashcards','ai_feedbacks':'writing_submissions','speaking_prompt_results':'speaking_attempts','question_options':'questions','practice_attempt_answers':'practice_attempts'}
BUS={'user_flashcard_progress','writing_submissions','speaking_attempts','practice_attempts'}  # their user_id edges share one bottom bus
COL={n:i for i,L in enumerate(LAY) for n in L};KEEP=set(COL)
GX,GAP,M=420,60,150
DROP=('created_at','updated_at','deleted_at')
def vis(n):
    t=T[n];fk={a for a,p in t['fks'] if p in KEEP};out=[]
    for c in t['cols']:
        if c[0] in fk or c[0] in t['pk']:out.append(c);continue
        if c[0] in DROP or 'image' in c[0] or 'author' in c[0] or c[0] in('audio_public_id','audio_source','model_name','sort_order'):continue
        out.append(c)
    keep=[c for c in out if c[0] in fk or c[0] in t['pk']];rest=[c for c in out if c not in keep]
    sel=set(map(id,keep))|set(map(id,rest[:max(0,8-len(keep))]))
    return [c for c in out if id(c) in sel]
V={n:vis(n) for n in KEEP}
h=lambda n:HH+len(V[n])*RH+FH
pos={}
for ci,col in enumerate(LAY):
    x=M+ci*(W+GX);y=M;
    for n in col:
        if n in ALIGN:y=max(y,pos[ALIGN[n]][1])
        pos[n]=(x,y);y+=h(n)+GAP
CW=M*2+len(LAY)*W+(len(LAY)-1)*GX
bottom=max(pos[n][1]+h(n) for n in pos)
YB=bottom+70;CH=YB+130
img=Image.new('RGB',(CW*S,CH*S),'#ffffff');d=ImageDraw.Draw(img)
for gx in range(0,CW,20):d.line([(gx*S,0),(gx*S,CH*S)],fill='#f0f0f0',width=1)
for gy in range(0,CH,20):d.line([(0,gy*S),(CW*S,gy*S)],fill='#f0f0f0',width=1)
BK='#000000'
def line(a,b,w=2):d.line([a,b],fill=BK,width=w*S)
def dash(p,q,on=14,off=8):
    (x1,y1),(x2,y2)=p,q;L=((x2-x1)**2+(y2-y1)**2)**.5
    if L==0:return
    ux,uy=(x2-x1)/L,(y2-y1)/L;t=0
    while t<L:
        e=min(t+on*S,L);d.line([(x1+ux*t,y1+uy*t),(x1+ux*e,y1+uy*e)],fill=BK,width=2*S);t+=(on+off)*S
def mark(pt,side,kind):
    x,y=pt;sg=1 if side=='R' else -1
    if kind=='many':
        line((x,y),(x+sg*16*S,y-8*S));line((x,y),(x+sg*16*S,y+8*S));line((x+sg*16*S,y-9*S),(x+sg*16*S,y+9*S))
        d.text((x+sg*30*S,y-14*S),'N',font=fL,fill=BK,anchor='mm')
    else:
        line((x+sg*10*S,y-9*S),(x+sg*10*S,y+9*S));line((x+sg*16*S,y-9*S),(x+sg*16*S,y+9*S))
        d.text((x+sg*30*S,y-14*S),'1',font=fL,fill=BK,anchor='mm')
def rowY(n,cn):
    i=[c[0] for c in V[n]].index(cn);return (pos[n][1]+HH+i*RH+RH/2)*S
import random,itertools
random.seed(1)
edges=[(c,a,p) for c in KEEP for a,p in T[c]['fks'] if p in KEEP]
LS=32
TRUNK={'users','topics'}
ytop=(M-70)*S;ybot=YB*S;xl=(M-70)*S
def gbase(g):return (M+(g+1)*W+g*GX+70)*S
ymed=sorted(pos[c][1] for c in BUS)[len(BUS)//2]
single={};trunk={};routes=[]
for c,a,p in edges:
    yc=rowY(c,a)
    if p=='users' and c in BUS:
        routes.append(dict(kind='bus',c=c,yc=yc,g=COL[c]-1,up=pos[c][1]<ymed));continue
    if p in TRUNK:trunk.setdefault(p,[]).append((c,yc));continue
    single.setdefault(p,[]).append((c,yc))
for p,L in single.items():   # attach as close to the child's row as possible
    lo=(pos[p][1]+HH+RH/2)*S;hi=(pos[p][1]+h(p)-FH-RH/2)*S;prev=None
    for c,yc in sorted(L,key=lambda e:e[1]):
        y=min(max(yc,lo),hi)
        if prev is not None and y<prev+14*S:y=prev+14*S
        routes.append(dict(kind='single',c=c,p=p,yc=yc,yp=y,g=COL[p]));prev=y
for p,L in trunk.items():
    routes.append(dict(kind='trunk',p=p,ch=L,g=COL[p],yp=(pos[p][1]+HH+RH/2)*S))
def build(r,k):
    xm=gbase(r['g'])+k*LS*S;sg=[];mk=[]
    if r['kind']=='bus':
        xc=pos[r['c']][0]*S;y=r['yc'];ye=ytop if r['up'] else ybot
        return [((xc,y),(xm,y)),((xm,y),(xm,ye))],[((xc,y),'L','many')]
    xp=(pos[r['p']][0]+W)*S
    if r['kind']=='single':
        xc=pos[r['c']][0]*S;y1,y2=r['yp'],r['yc'];mk=[((xc,y2),'L','many'),((xp,y1),'R','one')]
        if abs(y1-y2)<2:return [((xp,y2),(xc,y2))],mk
        return [((xp,y1),(xm,y1)),((xm,y1),(xm,y2)),((xm,y2),(xc,y2))],mk
    yp=r['yp'];ys=[yp]+[y for _,y in r['ch']];sg=[((xp,yp),(xm,yp)),((xm,min(ys)),(xm,max(ys)))];mk=[((xp,yp),'R','one')]
    for c,y in r['ch']:
        xc=pos[c][0]*S;sg.append(((xm,y),(xc,y)));mk.append(((xc,y),'L','many'))
    return sg,mk
def cross(A,B):
    n=0
    for (a1,a2) in A:
        for (b1,b2) in B:
            ah=a1[1]==a2[1];bh=b1[1]==b2[1]
            if ah==bh:
                if ah and abs(a1[1]-b1[1])<1 and min(max(a1[0],a2[0]),max(b1[0],b2[0]))>max(min(a1[0],a2[0]),min(b1[0],b2[0])):n+=3
                if (not ah) and abs(a1[0]-b1[0])<1 and min(max(a1[1],a2[1]),max(b1[1],b2[1]))>max(min(a1[1],a2[1]),min(b1[1],b2[1])):n+=3
                continue
            (h1,h2),(v1,v2)=((a1,a2),(b1,b2)) if ah else ((b1,b2),(a1,a2))
            if min(h1[0],h2[0])<v1[0]<max(h1[0],h2[0]) and min(v1[1],v2[1])<h1[1]<max(v1[1],v2[1]):n+=1
    return n
def needs_lane(r):return r['kind']!='single' or abs(r['yp']-r['yc'])>=2
for g in sorted({r['g'] for r in routes}):
    need=[r for r in routes if r['g']==g and needs_lane(r)]
    fixed=[build(r,0)[0] for r in routes if r['kind']=='single' and not needs_lane(r) and COL[r['p']]<=g<COL[r['c']]]
    def sc(ks):
        sg=[build(r,k)[0] for r,k in zip(need,ks)];t=0
        for i in range(len(sg)):
            for j in range(i+1,len(sg)):t+=cross(sg[i],sg[j])
            for f in fixed:t+=cross(sg[i],f)
        return t
    ks=list(range(len(need)));best=sc(ks)
    for _ in range(2500):
        if len(ks)<2:break
        i,j=random.sample(range(len(ks)),2);ks[i],ks[j]=ks[j],ks[i];v=sc(ks)
        if v<=best:best=v
        else:ks[i],ks[j]=ks[j],ks[i]
    for r,k in zip(need,ks):r['k']=k
    print('gutter',g,'routes',len(need),'crossings',best)
for r in routes:r['seg'],r['mk']=build(r,r.get('k',0))
V_=[(id(r),a,b) for r in routes for a,b in r['seg'] if a[0]==b[0]]
def hline(a,b,me):
    y=a[1];x1,x2=sorted((a[0],b[0]))
    cuts=sorted({v0[0] for (i,v0,v1) in V_ if i!=me and x1+8*S<v0[0]<x2-8*S and min(v0[1],v1[1])<y<max(v0[1],v1[1])})
    xs=x1
    for cx in cuts:
        dash((xs,y),(cx-7*S,y));d.arc([cx-7*S,y-7*S,cx+7*S,y+7*S],180,360,fill=BK,width=2*S);xs=cx+7*S
    dash((xs,y),(x2,y))
for r in routes:
    for a,b in r['seg']:
        if a[1]==b[1]:hline(a,b,id(r))
        else:dash(a,b)
    for pt,sd,kd in r['mk']:mark(pt,sd,kd)
yu=(pos['users'][1]+HH+RH/2+30)*S;bx=[r['seg'][1][0][0] for r in routes if r['kind']=='bus']
dash((xl,ytop),(max(bx),ytop)) if any(r['up'] for r in routes if r['kind']=='bus') else None
dash((xl,ybot),(max(bx),ybot));dash((xl,ytop),(xl,ybot));dash((xl,yu),(pos['users'][0]*S,yu));mark((pos['users'][0]*S,yu),'L','one')
def key(x,y,kind):
    x*=S;y*=S;r=5*S
    d.polygon([(x,y-r),(x+r,y),(x,y+r),(x-r,y)],fill={'pk':'#e8c32e','fk':'#d9534f','nn':'#4bb4c8','n':'#ffffff'}[kind],outline='#555555')
for n in KEEP:
    x,y=pos[n];t=T[n];hh=h(n);fks={a for a,p in t['fks'] if p in KEEP}
    d.rectangle([x*S,y*S,(x+W)*S,(y+hh)*S],fill='#ffffff',outline='#555555',width=S)
    d.rectangle([x*S,y*S,(x+W)*S,(y+HH)*S],fill='#8db8d8',outline='#555555',width=S)
    d.rectangle([x*S+S,y*S+S,(x+W)*S-S,(y+HH/2)*S],fill='#a3c6e0')
    d.text(((x+36)*S,(y+HH/2)*S),n,font=fH,fill='#111111',anchor='lm')
    d.polygon([((x+W-20)*S,(y+12)*S),((x+W-9)*S,(y+12)*S),((x+W-14.5)*S,(y+21)*S)],fill='#333333')
    d.rectangle([(x+11)*S,(y+10)*S,(x+26)*S,(y+23)*S],outline='#333333',width=S);d.line([((x+11)*S,(y+15)*S),((x+26)*S,(y+15)*S)],fill='#333333',width=S)
    for i,(cn,ct,nn) in enumerate(V[n]):
        yy=y+HH+i*RH+RH/2
        key(x+17,yy,'pk' if cn in t['pk'] else 'fk' if cn in fks else 'nn' if nn else 'n')
        tt='ENUM(...)' if ct.upper().startswith('ENUM') else ct.upper()
        d.text(((x+32)*S,yy*S),cn,font=fN,fill='#111111',anchor='lm')
        d.text(((x+W-10)*S,yy*S),tt,font=fT,fill='#6b6b6b',anchor='rm')
    fy=y+HH+len(V[n])*RH
    d.rectangle([x*S,fy*S,(x+W)*S,(y+hh)*S],fill='#c9c9c9',outline='#555555',width=S)
    d.text(((x+10)*S,(fy+FH/2)*S),'Indexes',font=fT,fill='#555555',anchor='lm')
ly=CH-45
d.text((M*S,ly*S),f'En-Learning ERD · {len(KEEP)} bảng chức năng chính · MySQL 9.0 · đường nối: 1 = phía một, N = phía nhiều · cột audit/ảnh được ẩn bớt',font=fN,fill='#333333',anchor='lm')
img.save('.docs/db/en-learning-erd.png');print(img.size)
