import React from "react";
import RadioButtons from "../RadioButtons";

export default {
  title: "Atoms/RadioButtons",
  component: RadioButtons,
};

const Template = (args) => <RadioButtons {...args} />;

export const Default = Template.bind({});
Default.args = {
  options: [
    { id: 1, name: "Option 1" },
    { id: 2, name: "Option 2" },
    { id: 3, name: "Option 3" },
  ],
  optionsKey: "name",
  selectedOption: null,
  onSelect: (selected) => console.log("Selected Option:", selected),
  innerStyles: {},
  style: {},
};

export const WithSelectedOption = Template.bind({});
WithSelectedOption.args = {
  options: [
    { id: 1, name: "Option 1" },
    { id: 2, name: "Option 2" },
    { id: 3, name: "Option 3" },
  ],
  optionsKey: "name",
  selectedOption: { id: 2, name: "Option 2" },
  onSelect: (selected) => console.log("Selected Option:", selected),
  innerStyles: {},
  style: {},
};

export const WithDisabledOption = Template.bind({});
WithDisabledOption.args = {
  options: [
    { id: 1, name: "Option 1" },
    { id: 2, name: "Option 2" },
    { id: 3, name: "Option 3" },
  ],
  optionsKey: "name",
  selectedOption: null,
  onSelect: (selected) => console.log("Selected Option:", selected),
  innerStyles: {},
  style: {},
  disabled: true,
};
