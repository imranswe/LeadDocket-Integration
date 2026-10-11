#!/usr/bin/env python3
"""Sign a synthetic local delivery; default destination is localhost only."""
import argparse,hashlib,hmac,os,urllib.request
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--url',default='http://localhost:8081/webhooks/leaddocket');p.add_argument('--payload',default=str(Path(__file__).resolve().parents[1]/'examples/webhook.json'));a=p.parse_args()
key=os.environ.get('LD_WEBHOOK_KEY')
if not key: p.error('Export LD_WEBHOOK_KEY matching the running app')
body=Path(a.payload).read_bytes();sig=hmac.new(key.encode(),body,hashlib.sha256).hexdigest()
r=urllib.request.Request(a.url,data=body,headers={'Content-Type':'application/json','x-leaddocket-signature':sig},method='POST')
with urllib.request.urlopen(r,timeout=10) as response: print(response.status,response.read().decode())
