import React from "react";
import { Festival } from "./Festival";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Festival",
  component: Festival,
};

export const Default = () => <Festival />;
export const Fill = () => <Festival fill="blue" />;
export const Size = () => <Festival height="50" width="50" />;
export const CustomStyle = () => <Festival style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Festival className="custom-class" />;

export const Clickable = () => <Festival onClick={()=>console.log("clicked")} />;

const Template = (args) => <Festival {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
