import React from "react";
import { Build } from "./Build";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Build",
  component: Build,
};

export const Default = () => <Build />;
export const Fill = () => <Build fill="blue" />;
export const Size = () => <Build height="50" width="50" />;
export const CustomStyle = () => <Build style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Build className="custom-class" />;

export const Clickable = () => <Build onClick={()=>console.log("clicked")} />;

const Template = (args) => <Build {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
