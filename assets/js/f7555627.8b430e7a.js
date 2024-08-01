"use strict";(self.webpackChunkdocs=self.webpackChunkdocs||[]).push([[359],{3905:(e,t,n)=>{n.d(t,{Zo:()=>s,kt:()=>b});var o=n(7294);function a(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function r(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var o=Object.getOwnPropertySymbols(e);t&&(o=o.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,o)}return n}function i(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?r(Object(n),!0).forEach((function(t){a(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):r(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function p(e,t){if(null==e)return{};var n,o,a=function(e,t){if(null==e)return{};var n,o,a={},r=Object.keys(e);for(o=0;o<r.length;o++)n=r[o],t.indexOf(n)>=0||(a[n]=e[n]);return a}(e,t);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);for(o=0;o<r.length;o++)n=r[o],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(a[n]=e[n])}return a}var l=o.createContext({}),d=function(e){var t=o.useContext(l),n=t;return e&&(n="function"==typeof e?e(t):i(i({},t),e)),n},s=function(e){var t=d(e.components);return o.createElement(l.Provider,{value:t},e.children)},c="mdxType",u={inlineCode:"code",wrapper:function(e){var t=e.children;return o.createElement(o.Fragment,{},t)}},m=o.forwardRef((function(e,t){var n=e.components,a=e.mdxType,r=e.originalType,l=e.parentName,s=p(e,["components","mdxType","originalType","parentName"]),c=d(n),m=a,b=c["".concat(l,".").concat(m)]||c[m]||u[m]||r;return n?o.createElement(b,i(i({ref:t},s),{},{components:n})):o.createElement(b,i({ref:t},s))}));function b(e,t){var n=arguments,a=t&&t.mdxType;if("string"==typeof e||a){var r=n.length,i=new Array(r);i[0]=m;var p={};for(var l in t)hasOwnProperty.call(t,l)&&(p[l]=t[l]);p.originalType=e,p[c]="string"==typeof e?e:a,i[1]=p;for(var d=2;d<r;d++)i[d]=n[d];return o.createElement.apply(null,i)}return o.createElement.apply(null,n)}m.displayName="MDXCreateElement"},8592:(e,t,n)=>{n.r(t),n.d(t,{assets:()=>l,contentTitle:()=>i,default:()=>u,frontMatter:()=>r,metadata:()=>p,toc:()=>d});var o=n(7462),a=(n(7294),n(3905));const r={sidebar_position:5},i="AdMob Setup",p={unversionedId:"development/admob-setup",id:"development/admob-setup",title:"AdMob Setup",description:"Create AdMob App",source:"@site/docs/development/admob-setup.md",sourceDirName:"development",slug:"/development/admob-setup",permalink:"/jay-android/docs/development/admob-setup",draft:!1,editUrl:"https://github.com/HLCaptain/jay-android/edit/main/docs/docs/development/admob-setup.md",tags:[],version:"current",lastUpdatedBy:"Bal\xe1zs P\xfcsp\xf6k-Kiss",lastUpdatedAt:1722532424,formattedLastUpdatedAt:"Aug 1, 2024",sidebarPosition:5,frontMatter:{sidebar_position:5},sidebar:"docsSidebar",previous:{title:"Mapbox Setup",permalink:"/jay-android/docs/development/mapbox-app-setup"},next:{title:"SonarCloud Setup",permalink:"/jay-android/docs/development/sonarcloud-setup"}},l={},d=[{value:"Create AdMob App",id:"create-admob-app",level:2},{value:"Add AdMob App ID to the Project",id:"add-admob-app-id-to-the-project",level:2},{value:"Create Ad Units",id:"create-ad-units",level:2}],s={toc:d},c="wrapper";function u(e){let{components:t,...n}=e;return(0,a.kt)(c,(0,o.Z)({},s,n,{components:t,mdxType:"MDXLayout"}),(0,a.kt)("h1",{id:"admob-setup"},"AdMob Setup"),(0,a.kt)("h2",{id:"create-admob-app"},"Create AdMob App"),(0,a.kt)("ol",null,(0,a.kt)("li",{parentName:"ol"},"Sign In to ",(0,a.kt)("a",{parentName:"li",href:"https://apps.admob.com/"},"Google AdMob"),"."),(0,a.kt)("li",{parentName:"ol"},"Link ",(0,a.kt)("a",{parentName:"li",href:"https://console.firebase.google.com/"},"Firebase")," to AdMob."),(0,a.kt)("li",{parentName:"ol"},"Link ",(0,a.kt)("a",{parentName:"li",href:"https://ads.google.com/"},"Google Ads")," to AdMob."),(0,a.kt)("li",{parentName:"ol"},"From side bar, click on ",(0,a.kt)("inlineCode",{parentName:"li"},"Apps")," and then ",(0,a.kt)("inlineCode",{parentName:"li"},"Add App"),". Fill in the details.")),(0,a.kt)("p",null,"Congratulations! You have created an AdMob app."),(0,a.kt)("h2",{id:"add-admob-app-id-to-the-project"},"Add AdMob App ID to the Project"),(0,a.kt)("ol",null,(0,a.kt)("li",{parentName:"ol"},"Select ",(0,a.kt)("inlineCode",{parentName:"li"},"Apps")," and then ",(0,a.kt)("inlineCode",{parentName:"li"},"View all apps"),"."),(0,a.kt)("li",{parentName:"ol"},"Copy the App ID into ",(0,a.kt)("inlineCode",{parentName:"li"},"local.properties")," with the key ",(0,a.kt)("inlineCode",{parentName:"li"},"ADMOB_APPLICATION_ID"),".")),(0,a.kt)("p",null,"Congratulations! You have connected your AdMob app to the project."),(0,a.kt)("h2",{id:"create-ad-units"},"Create Ad Units"),(0,a.kt)("p",null,"Without Ad Units, you can't show ads in your app. Here's how to create them:"),(0,a.kt)("ol",null,(0,a.kt)("li",{parentName:"ol"},"Click on the app you just created."),(0,a.kt)("li",{parentName:"ol"},"Click on ",(0,a.kt)("inlineCode",{parentName:"li"},"Ad Units")," and then ",(0,a.kt)("inlineCode",{parentName:"li"},"Add Ad Unit"),". Fill in the details."),(0,a.kt)("li",{parentName:"ol"},"You can copy the Ad Unit ID from the ",(0,a.kt)("inlineCode",{parentName:"li"},"Ad Units")," page and paste it into ",(0,a.kt)("inlineCode",{parentName:"li"},"Firebase Remote Config"),".")),(0,a.kt)("p",null,"You can access your Ad Unit IDs from ",(0,a.kt)("a",{parentName:"p",href:"https://firebase.google.com/docs/remote-config/"},"Firebase Remote Config")," and use them to initialize any Ads."))}u.isMDXComponent=!0}}]);