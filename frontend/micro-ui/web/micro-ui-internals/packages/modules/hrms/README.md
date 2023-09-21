<!-- TODO: update this -->

# digit-ui-module-hrms

## Install

```bash
npm install --save @egovernments/digit-ui-module-hrms
```

## Limitation

```bash
This Package is more specific to DIGIT-UI's can be used across mission's
```

## Usage

After adding the dependency make sure you have this dependency in

```bash
frontend/micro-ui/web/package.json
```

```json
"@egovernments/digit-ui-module-hrms":"^1.5.0",
```

then navigate to App.js

```bash
 frontend/micro-ui/web/src/App.js
```


```jsx
/** add this import **/

import { initHRMSComponents } from "@egovernments/digit-ui-module-hrms";

/** inside enabledModules add this new module key **/

const enabledModules = ["HRMS"];

/** inside init Function call this function **/

const initDigitUI = () => {
  initHRMSComponents();
};
```

# Changelog

```bash
1.8.0-beta.01 fixed compilation issue
1.8.0-beta workbench base version beta release
1.7.0 urban 2.9
1.6.0 urban 2.8
1.5.27 updated the readme content
1.5.26 some issue
1.5.25 corrected the bredcrumb issue
1.5.24 added the readme file
1.5.23 base version
```

# Contributors

[jagankumar-egov] [naveen-egov] [nipunarora-eGov] [Tulika-eGov] [Ramkrishna-egov] [vamshikrishnakole-wtt-egov] 

## Published from DIGIT Core 
Digit Dev Repo (https://github.com/egovernments/DIGIT-Dev/tree/digit-ui-core)

## License

MIT © [jagankumar-egov](https://github.com/jagankumar-egov)