"use strict";(self.webpackChunkdocs=self.webpackChunkdocs||[]).push([[42],{3905:(e,t,a)=>{a.d(t,{Zo:()=>c,kt:()=>g});var n=a(7294);function r(e,t,a){return t in e?Object.defineProperty(e,t,{value:a,enumerable:!0,configurable:!0,writable:!0}):e[t]=a,e}function o(e,t){var a=Object.keys(e);if(Object.getOwnPropertySymbols){var n=Object.getOwnPropertySymbols(e);t&&(n=n.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),a.push.apply(a,n)}return a}function l(e){for(var t=1;t<arguments.length;t++){var a=null!=arguments[t]?arguments[t]:{};t%2?o(Object(a),!0).forEach((function(t){r(e,t,a[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(a)):o(Object(a)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(a,t))}))}return e}function i(e,t){if(null==e)return{};var a,n,r=function(e,t){if(null==e)return{};var a,n,r={},o=Object.keys(e);for(n=0;n<o.length;n++)a=o[n],t.indexOf(a)>=0||(r[a]=e[a]);return r}(e,t);if(Object.getOwnPropertySymbols){var o=Object.getOwnPropertySymbols(e);for(n=0;n<o.length;n++)a=o[n],t.indexOf(a)>=0||Object.prototype.propertyIsEnumerable.call(e,a)&&(r[a]=e[a])}return r}var s=n.createContext({}),p=function(e){var t=n.useContext(s),a=t;return e&&(a="function"==typeof e?e(t):l(l({},t),e)),a},c=function(e){var t=p(e.components);return n.createElement(s.Provider,{value:t},e.children)},d="mdxType",u={inlineCode:"code",wrapper:function(e){var t=e.children;return n.createElement(n.Fragment,{},t)}},m=n.forwardRef((function(e,t){var a=e.components,r=e.mdxType,o=e.originalType,s=e.parentName,c=i(e,["components","mdxType","originalType","parentName"]),d=p(a),m=r,g=d["".concat(s,".").concat(m)]||d[m]||u[m]||o;return a?n.createElement(g,l(l({ref:t},c),{},{components:a})):n.createElement(g,l({ref:t},c))}));function g(e,t){var a=arguments,r=t&&t.mdxType;if("string"==typeof e||r){var o=a.length,l=new Array(o);l[0]=m;var i={};for(var s in t)hasOwnProperty.call(t,s)&&(i[s]=t[s]);i.originalType=e,i[d]="string"==typeof e?e:r,l[1]=i;for(var p=2;p<o;p++)l[p]=a[p];return n.createElement.apply(null,l)}return n.createElement.apply(null,a)}m.displayName="MDXCreateElement"},6308:(e,t,a)=>{a.r(t),a.d(t,{assets:()=>s,contentTitle:()=>l,default:()=>u,frontMatter:()=>o,metadata:()=>i,toc:()=>p});var n=a(7462),r=(a(7294),a(3905));const o={slug:"welcome",title:"Welcome",authors:"illyan",tags:["Hello World!"]},l=void 0,i={permalink:"/jay-android/blog/welcome",editUrl:"https://github.com/HLCaptain/jay-android/tree/main/docs/blog/blog/2023-03-22-welcome/index.md",source:"@site/blog/2023-03-22-welcome/index.md",title:"Welcome",description:"This site was created to document changes and have a landing page of the Android version of Jay.",date:"2023-03-22T00:00:00.000Z",formattedDate:"March 22, 2023",tags:[{label:"Hello World!",permalink:"/jay-android/blog/tags/hello-world"}],readingTime:1.18,hasTruncateMarker:!1,authors:[{name:"Bal\xe1zs P\xfcsp\xf6k-Kiss",title:"Lead Developer",url:"https://github.com/HLCaptain",imageURL:"https://github.com/hlcaptain.png",key:"illyan"}],frontMatter:{slug:"welcome",title:"Welcome",authors:"illyan",tags:["Hello World!"]}},s={authorsImageUrls:[void 0]},p=[{value:"Features",id:"features",level:2},{value:"Plans",id:"plans",level:2}],c={toc:p},d="wrapper";function u(e){let{components:t,...a}=e;return(0,r.kt)(d,(0,n.Z)({},c,a,{components:t,mdxType:"MDXLayout"}),(0,r.kt)("p",null,"This site was created to ",(0,r.kt)("strong",{parentName:"p"},"document changes")," and have a ",(0,r.kt)("strong",{parentName:"p"},"landing page")," of the Android version of ",(0,r.kt)("strong",{parentName:"p"},"Jay"),"."),(0,r.kt)("p",null,"I plan to experiment with ",(0,r.kt)("strong",{parentName:"p"},"blog posts"),", like ",(0,r.kt)("em",{parentName:"p"},"this"),", to get the hang of managing this side of a production ready-ish app. A brief description of Jay, and what the project is aiming for:"),(0,r.kt)("p",null,"Jay is a data analytics solution, which aims to make data analytics more accessible for ",(0,r.kt)("strong",{parentName:"p"},(0,r.kt)("em",{parentName:"strong"},"Everyone")),". The Android app collects data on-device, which can be uploaded to the cloud to be analyzed and make local decisions on local problems."),(0,r.kt)("h2",{id:"features"},"Features"),(0,r.kt)("ul",null,(0,r.kt)("li",{parentName:"ul"},"Collect and display sensory and location data"),(0,r.kt)("li",{parentName:"ul"},"Upload and sync data in the cloud"),(0,r.kt)("li",{parentName:"ul"},"Support multiple users on a single device"),(0,r.kt)("li",{parentName:"ul"},"Provide an easy-to-use and good looking interface"),(0,r.kt)("li",{parentName:"ul"},"Be cheap to implement and maintain"),(0,r.kt)("li",{parentName:"ul"},"Analyze data")),(0,r.kt)("h2",{id:"plans"},"Plans"),(0,r.kt)("ul",null,(0,r.kt)("li",{parentName:"ul"},"digitally generate and label data, which then can be used to train machine learning models"),(0,r.kt)("li",{parentName:"ul"},"analyze/process data on-device with ML models"),(0,r.kt)("li",{parentName:"ul"},"enable users to have supervisors for B2B applications"),(0,r.kt)("li",{parentName:"ul"},"make an administration app/webpage to supervise users"),(0,r.kt)("li",{parentName:"ul"},"make even more tools and solutions accessible, be as transparent as possible during development")))}u.isMDXComponent=!0}}]);