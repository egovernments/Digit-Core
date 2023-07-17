import React from "react";
import { Restaurant } from "./Restaurant";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Restaurant",
  component: Restaurant,
};

export const Default = () => <Restaurant />;
export const Fill = () => <Restaurant fill="blue" />;
export const Size = () => <Restaurant height="50" width="50" />;
export const CustomStyle = () => <Restaurant style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Restaurant className="custom-class" />;

export const Clickable = () => <Restaurant onClick={()=>console.log("clicked")} />;

const Template = (args) => <Restaurant {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
