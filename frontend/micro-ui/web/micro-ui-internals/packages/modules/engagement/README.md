<!-- TODO: update this -->

# digit-ui-module-engagement

## Install

```bash
npm install --save @egovernments/digit-ui-module-engagement
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
"@egovernments/digit-ui-module-engagement":"^1.5.0",
```

then navigate to App.js

```bash
 frontend/micro-ui/web/src/App.js
```


```jsx
/** add this import **/

import { initEngagementComponents } from "@egovernments/digit-ui-module-engagement";

/** inside enabledModules add this new module key **/

const enabledModules = ["Engagement"];

/** inside init Function call this function **/

const initDigitUI = () => {
  initEngagementComponents();
};
```

# Changelog

```bash
1.8.0-beta workbench base version beta release
1.7.0 urban 2.9
1.6.0 urban 2.8
1.5.25 updated the readme content
1.5.24 added the readme file
1.5.23 base version
```

# Contributors

[jagankumar-egov] [nipunarora-eGov] [Tulika-eGov] [Ramkrishna-egov] [vamshikrishnakole-wtt-egov] 

## Published from DIGIT Core 
Digit Dev Repo (https://github.com/egovernments/Digit-Core/tree/digit-ui-core)

## License

MIT © [jagankumar-egov](https://github.com/jagankumar-egov)