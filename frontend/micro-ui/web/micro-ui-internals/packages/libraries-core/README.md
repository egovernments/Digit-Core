# digit-ui-libraries-core

> Made with @egovernments/create-ui-library-core

## Install

```bash
npm install --save @egovernments/digit-ui-libraries-core
```

## Usage

```jsx
import React from "react";
import initCoreLibraries from "@egovernments/digit-ui-libraries-core";

import defaultConfig from "./config";

const App = ({ deltaConfig, stateCode, cityCode, moduleCode }) => {
  initCoreLibraries();

  const store = eGov.Services.useStore(defaultConfig, { deltaConfig, stateCode, cityCode, moduleCode });

  return <p>Create React Library Example 😄</p>;
};

export default App;
```

## License

MIT © [](https://github.com/)
