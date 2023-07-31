import React from "react";
import { LoaderScreen } from "../LoaderScreen";

export default {
  title: "Atoms/LoaderScreen",
  component: LoaderScreen,
  argTypes: {
    page: {
      control: "boolean",
    },
  },
};

const Template = (args) => <LoaderScreen {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  page: true,
};

export const Primary = Template.bind({});
