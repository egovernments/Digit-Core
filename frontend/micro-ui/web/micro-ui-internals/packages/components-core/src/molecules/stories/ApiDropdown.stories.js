import React from "react";
import { QueryClient, QueryClientProvider } from "react-query";
import MultiSelectDropdown from "../../atoms/MultiSelectDropdown";
import Dropdown from "../../atoms/Dropdown";

export default {
  title: "Molecules/ApiDropdown",
};

const queryClient = new QueryClient();

const options = [
  { id: 1, name: "Option 1" },
  { id: 2, name: "Option 2" },
  { id: 3, name: "Option 3" },
];

const Template = (args) => (
  <QueryClientProvider client={queryClient}>
    {/* Conditionally render MultiSelectDropdown or Dropdown based on props */}
    {args.isMultiSelect ? (
      <MultiSelectDropdown
        {...args}
        onSelect={(selectedOptions) => {
          console.log("Selected options:", selectedOptions);
        }}
      />
    ) : (
      <Dropdown {...args} />
    )}
  </QueryClientProvider>
);

export const MultiSelectDropdownStory = Template.bind({});
MultiSelectDropdownStory.args = {
  isMultiSelect: true,
  populators: {},
  formData: {},
  props: {},
  inputRef: null,
  errors: {},
  setValue: () => {},
  options,
  optionsKey: "name",
};

export const DropdownStory = Template.bind({});
DropdownStory.args = {
  populators: {},
  formData: {},
  props: {},
  inputRef: null,
  errors: {},
  setValue: () => {},
  option: options,
  optionKey: "name",
};
