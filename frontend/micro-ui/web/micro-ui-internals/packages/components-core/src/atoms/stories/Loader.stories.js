import React from "react";
import { Loader } from "../Loader";

export default {
  title: "Atoms/Loader",
  component: Loader,
  argTypes: {
    page: {
      control: "boolean",
    },
  },
};

const Template = (args) => <Loader {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  page: true,
};

export const Primary = Template.bind({});
