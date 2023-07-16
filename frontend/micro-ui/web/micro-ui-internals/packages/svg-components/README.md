

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

Syntax for importing any component;

```jsx
import React, { Component } from "react";
import { Accessibility } from "@egovernments/digit-ui-svg-components";

class Example extends Component {
  render() {
    return <Accessibility />;
  }
}
```

# Changelog

```bash
0.0.1 base version
```

## Published from DIGIT Core 
Digit Dev Repo (https://github.com/egovernments/DIGIT-Dev/tree/digit-ui-core)

## License

MIT © [nabeelmd-egov](https://github.com/nabeelmd-egov)
