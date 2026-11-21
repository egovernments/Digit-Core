import React from "react";
import { FilterAlt } from "./FilterAlt";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "FilterAlt",
  component: FilterAlt,
};

export const Default = () => <FilterAlt />;
export const Fill = () => <FilterAlt fill="blue" />;
export const Size = () => <FilterAlt height="50" width="50" />;
export const CustomStyle = () => <FilterAlt style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <FilterAlt className="custom-class" />;

export const Clickable = () => <FilterAlt onClick={()=>console.log("clicked")} />;

const Template = (args) => <FilterAlt {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
