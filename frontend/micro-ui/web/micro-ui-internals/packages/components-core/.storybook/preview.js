// import "@egovernments/digit-ui-css/example/index.css";
import "@egovernments/digit-ui-css/dist/index.css";
import { initCoreLibraries } from "@egovernments/digit-ui-libraries-core";

// import '../src/index.css';
//👇 Configures Storybook to log the actions( onArchiveTask and onPinTask ) in the UI.
/** @type { import('@storybook/react').Preview } */

export const globalTypes = {
  theme: {
    description: "Global theme for components",
    defaultValue: "light",
    toolbar: {
      // The label to show for this toolbar item
      title: "Theme",
      icon: "circlehollow",
      // Array of plain string values or MenuItem shape (see below)
      items: ["light", "dark"],
      // Change title based on selected value
      dynamicTitle: true,
    },
  },
};

const preview = {
  parameters: {
    actions: { argTypesRegex: "^on[A-Z].*" },
    controls: {
      expanded: true,
      matchers: {
        color: /(background|color)$/i,
        date: /Date$/,
      },
    },
    backgrounds: {
      default: "twitter",
      values: [
        {
          name: "twitter",
          value: "#00aced",
        },
        {
          name: "facebook",
          value: "#3b5998",
        },
      ],
    },
  },
};

initCoreLibraries().then(() => {
  console.info("DIGIT Contant, HOOKS enabled", window?.Digit);
});

export default preview;
