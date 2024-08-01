"use strict";(self.webpackChunkdocs=self.webpackChunkdocs||[]).push([[812],{3905:(e,t,n)=>{n.d(t,{Zo:()=>c,kt:()=>h});var a=n(7294);function r(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function o(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);t&&(a=a.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,a)}return n}function i(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?o(Object(n),!0).forEach((function(t){r(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):o(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function l(e,t){if(null==e)return{};var n,a,r=function(e,t){if(null==e)return{};var n,a,r={},o=Object.keys(e);for(a=0;a<o.length;a++)n=o[a],t.indexOf(n)>=0||(r[n]=e[n]);return r}(e,t);if(Object.getOwnPropertySymbols){var o=Object.getOwnPropertySymbols(e);for(a=0;a<o.length;a++)n=o[a],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(r[n]=e[n])}return r}var s=a.createContext({}),p=function(e){var t=a.useContext(s),n=t;return e&&(n="function"==typeof e?e(t):i(i({},t),e)),n},c=function(e){var t=p(e.components);return a.createElement(s.Provider,{value:t},e.children)},d="mdxType",u={inlineCode:"code",wrapper:function(e){var t=e.children;return a.createElement(a.Fragment,{},t)}},m=a.forwardRef((function(e,t){var n=e.components,r=e.mdxType,o=e.originalType,s=e.parentName,c=l(e,["components","mdxType","originalType","parentName"]),d=p(n),m=r,h=d["".concat(s,".").concat(m)]||d[m]||u[m]||o;return n?a.createElement(h,i(i({ref:t},c),{},{components:n})):a.createElement(h,i({ref:t},c))}));function h(e,t){var n=arguments,r=t&&t.mdxType;if("string"==typeof e||r){var o=n.length,i=new Array(o);i[0]=m;var l={};for(var s in t)hasOwnProperty.call(t,s)&&(l[s]=t[s]);l.originalType=e,l[d]="string"==typeof e?e:r,i[1]=l;for(var p=2;p<o;p++)i[p]=n[p];return a.createElement.apply(null,i)}return a.createElement.apply(null,n)}m.displayName="MDXCreateElement"},7895:(e,t,n)=>{n.r(t),n.d(t,{assets:()=>s,contentTitle:()=>i,default:()=>u,frontMatter:()=>o,metadata:()=>l,toc:()=>p});var a=n(7462),r=(n(7294),n(3905));const o={slug:"ml-update",title:"Machine Learning Update",authors:"illyan",tags:["Machine Learning","Road to 1.0"]},i=void 0,l={permalink:"/jay-android/blog/ml-update",editUrl:"https://github.com/HLCaptain/jay-android/edit/main/docs/blog/2024-08-01-ml-update/index.md",source:"@site/blog/2024-08-01-ml-update/index.md",title:"Machine Learning Update",description:"I finally had the time to finish this huge update, what I started developing more than a year ago: on-device machine learning. Users now can analyze device sensor data with an on-device machine learning algorithm to predict driver aggression.",date:"2024-08-01T00:00:00.000Z",formattedDate:"August 1, 2024",tags:[{label:"Machine Learning",permalink:"/jay-android/blog/tags/machine-learning"},{label:"Road to 1.0",permalink:"/jay-android/blog/tags/road-to-1-0"}],readingTime:.685,hasTruncateMarker:!1,authors:[{name:"Bal\xe1zs P\xfcsp\xf6k-Kiss",title:"Lead Developer",url:"https://github.com/HLCaptain",imageURL:"https://github.com/hlcaptain.png",key:"illyan"}],frontMatter:{slug:"ml-update",title:"Machine Learning Update",authors:"illyan",tags:["Machine Learning","Road to 1.0"]},nextItem:{title:"Welcome",permalink:"/jay-android/blog/welcome"}},s={authorsImageUrls:[void 0]},p=[{value:"Plans",id:"plans",level:2}],c={toc:p},d="wrapper";function u(e){let{components:t,...n}=e;return(0,r.kt)(d,(0,a.Z)({},c,n,{components:t,mdxType:"MDXLayout"}),(0,r.kt)("p",null,"I finally had the time to finish this huge update, what I started developing more than a year ago: ",(0,r.kt)("strong",{parentName:"p"},"on-device machine learning"),". Users now can analyze device sensor data with an on-device machine learning algorithm to predict ",(0,r.kt)("strong",{parentName:"p"},"driver aggression"),"."),(0,r.kt)("p",null,"The ML model can be downloaded via ",(0,r.kt)("a",{parentName:"p",href:"https://firebase.google.com/products/ml"},"Firebase ML service"),". The development of the ",(0,r.kt)("strong",{parentName:"p"},"model")," is also fully ",(0,r.kt)("strong",{parentName:"p"},"open-sourced")," and available under the ",(0,r.kt)("a",{parentName:"p",href:"https://github.com/HLCaptain/hawk-ai"},"Hawk AI GitHub repository")," with scripts to train and convert the model to the TensorFlow Lite format."),(0,r.kt)("h2",{id:"plans"},"Plans"),(0,r.kt)("ul",null,(0,r.kt)("li",{parentName:"ul"},"Rework session screen with better data visualization.")),(0,r.kt)("p",null,"For the foreseeable future, I don't plan to add more core features to the app, but when something comes up, I will resolve any issues."),(0,r.kt)("p",null,"I hope this will change in the future, but for now, enjoy what I consider a solid open-source base for Android application development."))}u.isMDXComponent=!0}}]);