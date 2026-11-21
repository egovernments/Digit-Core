import React from "react";
import { Shop } from "./Shop";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Shop",
  component: Shop,
};

export const Default = () => <Shop />;
export const Fill = () => <Shop fill="blue" />;
export const Size = () => <Shop height="50" width="50" />;
export const CustomStyle = () => <Shop style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Shop className="custom-class" />;

export const Clickable = () => <Shop onClick={()=>console.log("clicked")} />;

const Template = (args) => <Shop {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
