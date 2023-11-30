

<!-- TODO: update this -->

# digit-ui-react-components

## Install

```bash
npm install --save @egovernments/digit-ui-react-components
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
"@egovernments/digit-ui-react-components":"1.5.24",
```

then navigate to App.js

```bash
 frontend/micro-ui/web/src/App.js
```

Syntax for importing any component;

```jsx
import React, { Component } from "react";
import MyComponent from "@egovernments/digit-ui-react-components";

class Example extends Component {
  render() {
    return <MyComponent />;
  }
}
```
Syntax for the Inbox Composers

```jsx
    import { InboxSearchComposer } from "@egovernments/digit-ui-react-components";

    <React.Fragment>
      <Header className="works-header-search">{t(updatedConfig?.label)}</Header>
      <div className="inbox-search-wrapper">
        <InboxSearchComposer configs={updatedConfig}></InboxSearchComposer>
      </div>
    </React.Fragment>
```

Syntax for the FormComposersV2

```jsx
    import { FormComposerV2 as FormComposer } from "@egovernments/digit-ui-react-components";

   <React.Fragment>
      <Header >{t("CREATE_HEADER")}</Header>
      <FormComposer
        label={t("PROCEED")}
        config={configs.map((config) => {
          return {
            ...config,
            body: config.body.filter((a) => !a.hideInEmployee),
          };
        })}
        defaultValues={sessionFormData}
        onFormValueChange={onFormValueChange}
        onSubmit={onFormSubmit}
        fieldStyle={{ marginRight: 0 }}
        className="form-no-margin"
        labelBold={true}
      />
    </React.Fragment>
```
 

# Changelog

```bash
1.8.0-beta.1 fixed some topbar issues
1.8.0-beta workbench base version beta release
1.7.0 urban 2.9
1.6.0 urban 2.8
1.5.36 added classname for topbar options
1.5.35 aligment issue in edit and logout
1.5.34 updated login scss and alignment issues
1.5.28 passing response data in case for custom component in inbox composer
1.5.27 integrated with the svg library
1.5.26 fixed shipping truck icon fill issue
1.5.25 added new icon and fixed other issues
1.5.24 added and updated the Readme file
1.5.23 base version
```

# Contributors

[jagankumar-egov] [nipunarora-eGov] [Tulika-eGov] [Ramkrishna-egov] [nabeelmd-eGov] [anil-egov] [vamshikrishnakole-wtt-egov] 

## Published from DIGIT Core 
Digit Dev Repo (https://github.com/egovernments/DIGIT-Dev/tree/digit-ui-core)

## License

MIT © [jagankumar-egov](https://github.com/jagankumar-egov)
