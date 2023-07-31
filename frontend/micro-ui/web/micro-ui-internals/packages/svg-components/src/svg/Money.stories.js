import React from "react";
import { Money } from "./Money";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Money",
  component: Money,
};

export const Default = () => <Money />;
export const Fill = () => <Money fill="blue" />;
export const Size = () => <Money height="50" width="50" />;
export const CustomStyle = () => <Money style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Money className="custom-class" />;

export const Clickable = () => <Money onClick={()=>console.log("clicked")} />;

const Template = (args) => <Money {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
