import React from "react";
import { QueryClient, QueryClientProvider } from "react-query";
import LocationDropdownWrapper from "../LocationDropdownWrapper"; // Replace with your component import

export default {
  title: "Molecules/LocationDropdownWrapper",
  component: LocationDropdownWrapper,
};

// Simulate formData
const mockFormData = {
  ward: [{ code: "wardCode1" }, { code: "wardCode2" }],
};

const queryClient = new QueryClient();

// Simulate populators for your component

const options = [
  { id: 1, name: "Option 1" },
  { id: 2, name: "Option 2" },
  { id: 3, name: "Option 3" },
];

const mockPopulators = {
  type: "locality",
  allowMultiSelect: true,
  optionsKey: "name",
  defaultText: "Select an option",
  selectedText: "Selected",
  defaultValue: "",
  optionsCustomStyle: {},
  options: options,
};

const Template = (args) => (
  <QueryClientProvider client={queryClient}>
    <LocationDropdownWrapper
      formData={mockFormData}
      populators={mockPopulators}
      {...args}
      props={{
        onChange: (value) => {
          console.log("Value changed:", value);
        },
        value: ["localityCode1"],
      }}
    />
  </QueryClientProvider>
);

export const LocalityMultiSelectDropdown = Template.bind({});
LocalityMultiSelectDropdown.args = {
  allowMultiSelect: true,
};

// Story for Single Select Dropdown
const SingleSelectTemplate = (args) => (
  <QueryClientProvider client={queryClient}>
    <LocationDropdownWrapper
      formData={mockFormData}
      populators={{
        ...mockPopulators,
        allowMultiSelect: false,
      }}
      {...args}
      setValue={args.setValue} // Pass the setValue prop here
      onChange={args.onChange} // Pass the onChange prop here
      {...args}
      props={{
        onSelect: (selectedValues) => {
          if (mockPopulators.type === "ward") {
            args.setValue("locality", []); // Reset the value for specific condition
          }
          args.onChange(
            selectedValues
              ?.map((row) => {
                return row?.[1] ? row[1] : null;
              })
              .filter((e) => e)
          );
        },
      }}
    />
  </QueryClientProvider>
);

export const LocalitySingleSelectDropdown = SingleSelectTemplate.bind({});
LocalitySingleSelectDropdown.args = {
  allowMultiSelect: true,
  options: options,

  optionKey: "name",
  setValue: () => {},
  onChange: () => {},
};
