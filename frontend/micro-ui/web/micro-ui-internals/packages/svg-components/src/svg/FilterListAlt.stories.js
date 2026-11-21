import React from "react";
import { FilterListAlt } from "./FilterListAlt";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "FilterListAlt",
  component: FilterListAlt,
};

export const Default = () => <FilterListAlt />;
export const Fill = () => <FilterListAlt fill="blue" />;
export const Size = () => <FilterListAlt height="50" width="50" />;
export const CustomStyle = () => <FilterListAlt style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <FilterListAlt className="custom-class" />;

export const Clickable = () => <FilterListAlt onClick={()=>console.log("clicked")} />;

const Template = (args) => <FilterListAlt {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
