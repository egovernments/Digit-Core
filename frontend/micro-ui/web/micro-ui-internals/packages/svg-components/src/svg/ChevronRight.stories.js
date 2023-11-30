import React from "react";
import { ChevronRight } from "./ChevronRight";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ChevronRight",
  component: ChevronRight,
};

export const Default = () => <ChevronRight />;
export const Fill = () => <ChevronRight fill="blue" />;
export const Size = () => <ChevronRight height="50" width="50" />;
export const CustomStyle = () => <ChevronRight style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ChevronRight className="custom-class" />;

export const Clickable = () => <ChevronRight onClick={()=>console.log("clicked")} />;

const Template = (args) => <ChevronRight {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
