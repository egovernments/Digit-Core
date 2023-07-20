import React from "react";
import { Category } from "./Category";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Category",
  component: Category,
};

export const Default = () => <Category />;
export const Fill = () => <Category fill="blue" />;
export const Size = () => <Category height="50" width="50" />;
export const CustomStyle = () => <Category style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Category className="custom-class" />;

export const Clickable = () => <Category onClick={()=>console.log("clicked")} />;

const Template = (args) => <Category {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
