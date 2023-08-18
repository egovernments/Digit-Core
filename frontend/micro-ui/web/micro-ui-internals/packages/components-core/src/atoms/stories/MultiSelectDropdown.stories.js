import React from "react";
import MultiSelectDropdown from "../MultiSelectDropdown";
import { action } from "@storybook/addon-actions";

export default {
  title: "Atoms/MultiSelectDropdown",
  component: MultiSelectDropdown,
  argTypes: {
    options: { control: "object" },
    selected: { control: "array" },
    defaultLabel: { control: "text" },
    defaultUnit: { control: "text" },
    BlockNumber: { control: "number" },
    isOBPSMultiple: { control: "boolean" },
    isPropsNeeded: { control: "boolean" },
    ServerStyle: { control: "object" },
    config: { control: "object" },
  },
};

const options = [
  { id: 1, name: "Option 1" },
  { id: 2, name: "Option 2" },
  { id: 3, name: "Option 3" },
];

const onSelectAction = action("onSelect");

const Template = (args) => (
  <MultiSelectDropdown
    {...args}
    onSelect={(selectedOptions) => {
      onSelectAction(selectedOptions);
      console.log("Selected Options:", selectedOptions);
    }}
  />
);

export const Default = Template.bind({});
Default.args = {
  options,
  optionsKey: "name",
  selected: [],
  defaultLabel: "Select Options",
};

export const WithSelectedOptions = Template.bind({});
WithSelectedOptions.args = {
  options,
  optionsKey: "name",
  selected: [options[0], options[2]],
  defaultLabel: "Select Options",
};
