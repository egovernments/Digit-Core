<!-- TODO: update this -->

# digit-ui-svg-components

## Install

```bash
npm install --save @egovernments/digit-ui-svg-components
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
"@egovernments/digit-ui-svg-components":"0.0.1",
```

then navigate to App.js

```bash
 frontend/micro-ui/web/src/App.js
```

# SVGs can be imported from svg component libraries and also from digit-ui-react-components^1.5.27 package

Syntax for import any svgs

# from svg library

```jsx
import { Accessibility } from "@egovernments/digit-ui-svg-components";

<Accessibility />;
```

# from digit-ui-react-components

```jsx
import { SVG } from "@egovernments/digit-ui-react-components";

<SVG.Accessibility />;

or;

import { SVG } from "@egovernments/digit-ui-react-components";

const { Accessibility } = SVG;
<Accessibility />;
```

# Changelog

```bash
1.0.0-beta workbench base version beta release
0.0.8 Added RoundedCheck Icon and DeleteBtn icon with stories
0.0.7 Added Info banner icon
0.0.6 Added TickMark, Success Svg and fixed publish related issue
0.0.5 fixed publish related issue
0.0.4 fixed some fill related issue
0.0.3 Added NoResultsFoundicon,StarFilled , Star Empty and Fixed issue on  NoResultsFoundicon
0.0.2 added the prototypes and playground stories for all svgs
0.0.1 base version
```

## Published from DIGIT Core

Digit Core Repo (https://github.com/egovernments/Core-Platform/tree/digit-ui-core)

# Contributors

[nabeelmd-egov] [anil-egov] [jagankumar-egov]

# Reference

Home Page (https://unified-dev.digit.org/storybook-svg/)

## License

MIT © [jagankumar-egov](https://github.com/jagankumar-egov)
