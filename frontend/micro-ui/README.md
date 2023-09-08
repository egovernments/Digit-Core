
# workbench ui 

A React App built on top of DIGIT UI Core.


# DIGIT UI 

DIGIT (Digital Infrastructure for Governance, Impact & Transformation) is India's largest platform for governance services. Visit https://www.digit.org for more details.

This repository contains source code for web implementation of the new Digit UI modules with dependencies and libraries.

Workbench module is used to Manage the master data (MDMS V2 Service) used across the DIGIT Services / Applications

It is also used to manage the Localisation data present in the system (Localisation service)


## Run Locally

Clone the project

```bash
  git clone https://github.com/egovernments/Digit-Core.git
```

Go to the Sub directory to run UI
```bash
    cd into frontend/micro-ui/web/micro-ui-internals
```

Install dependencies

```bash
  yarn install
```

Add .env file
```bash
    frontend/micro-ui/web/micro-ui-internals/example/.env
```

Start the server

```bash
  yarn start
```


## Environment Variables

To run this project, you will need to add the following environment variables to your .env file

`REACT_APP_PROXY_API` ::  `{{server url}}`

`REACT_APP_GLOBAL`  ::  `{{server url}}`

`REACT_APP_PROXY_ASSETS`  ::  `{{server url}}`

`REACT_APP_USER_TYPE`  ::  `{{EMPLOYEE||CITIZEN}}`

`SKIP_PREFLIGHT_CHECK` :: `true`

[sample .env file](https://github.com/egovernments/Digit-Core/blob/workbench/frontend/micro-ui/web/micro-ui-internals/example/.env-unifieddev)

## Tech Stack

**Libraries:** 

[React](https://react.dev/)

[React Hook Form](https://www.react-hook-form.com/)

[React Query](https://tanstack.com/query/v3/)

[Tailwind CSS](https://tailwindcss.com/)

[Webpack](https://webpack.js.org/)

## License

[MIT](https://choosealicense.com/licenses/mit/)


## Author

- [@jagankumar-egov](https://www.github.com/jagankumar-egov)


## Documentation

[Documentation](https://https://core.digit.org/guides/developer-guide/ui-developer-guide/digit-ui)


## Support

For support, add the issues in https://github.com/egovernments/DIGIT-core/issues.


![Logo](https://s3.ap-south-1.amazonaws.com/works-dev-asset/mseva-white-logo.png)

