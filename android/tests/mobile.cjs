const {chromium}=require('playwright');
const path=require('path');const assert=require('assert');
(async()=>{
const browser=await chromium.launch({headless:true,args:['--no-sandbox']});const page=await browser.newPage({viewport:{width:390,height:844}});
const base=path.resolve(__dirname,'../app/src/main/assets');
await page.route('https://test.local/**',async route=>{
const name=new URL(route.request().url()).pathname.slice(1)||'mobile.html';
if(name==='firebase-app.js')return route.fulfill({contentType:'text/javascript',body:'export const initializeApp=()=>({});'});
if(name==='firebase-auth.js')return route.fulfill({contentType:'text/javascript',body:'export const getAuth=()=>({currentUser:{uid:"test"}});export const signInAnonymously=async()=>{};export const onAuthStateChanged=(a,fn)=>{queueMicrotask(fn);return ()=>{};};'});
if(name==='firebase-firestore.js')return route.fulfill({contentType:'text/javascript',body:`
let data=[{id:'p1',name:'Papel Bónd A4',barcode:'775123',price:12.5,stock:30,location:'Estante B',description:'Blanco 80 g',measureUnit:'Paquetes',category:'Producto'},{id:'p2',name:'<script>alert(1)</script>',barcode:'0009',price:1,stock:4}];let subscriber;
export const getFirestore=()=>({});export const enableIndexedDbPersistence=async()=>{};export const collection=()=>({});export const doc=()=>({id:'new1'});
function emit(){subscriber({docs:data.map(p=>({id:p.id,data:()=>p})),metadata:{fromCache:false,hasPendingWrites:false}});}
export const onSnapshot=(a,b,fn)=>{subscriber=fn;emit();};export const setDoc=async(ref,p)=>{window.saved=p;data.push(p);emit();};
`});
return route.fulfill({path:path.join(base,name)});
});
await page.goto('https://test.local/mobile.html');await page.waitForFunction(()=>document.getElementById('status').textContent==='● En línea');
assert.equal(await page.locator('.card').count(),2);
await page.locator('#query').fill('bond');assert.equal(await page.locator('.card').count(),1);
await page.locator('#query').fill('775123');await page.locator('#searchForm').evaluate(f=>f.requestSubmit());await page.locator('#detail').waitFor({state:'visible'});assert((await page.locator('#detailContent').innerText()).includes('Estante B'));
await page.locator('#backDetail').click();await page.evaluate(()=>window.receiveScan('775123'));await page.locator('#detail').waitFor({state:'visible'});
await page.locator('#backDetail').click();await page.locator('#query').fill('');await page.screenshot({path:'android/tests/mobile-home.png',fullPage:true});
assert.equal(await page.evaluate(()=>document.documentElement.scrollWidth>window.innerWidth),false);
await page.locator('#add').click();await page.locator('[name=name]').fill('Producto de prueba');await page.locator('[name=barcode]').fill('775123');await page.locator('[name=price]').fill('10');await page.locator('#save').click();assert((await page.locator('#notice').innerText()).includes('ya existe'));
await page.locator('[name=barcode]').fill('NEW01');await page.locator('[name=stock]').fill('7');await page.locator('#save').click();await page.waitForFunction(()=>!!window.saved);assert.equal(await page.evaluate(()=>window.saved.stock),7);assert.equal(await page.evaluate(()=>window.saved.category),'Producto');assert.equal(await page.locator('.card').count(),3);
await browser.close();console.log('PASS: search, accent normalization, exact code, native scan callback, details, duplicate code, product schema, no horizontal overflow.');
})().catch(e=>{console.error(e);process.exit(1)});
