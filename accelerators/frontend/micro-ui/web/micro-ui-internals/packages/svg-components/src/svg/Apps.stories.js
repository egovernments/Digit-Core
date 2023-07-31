import React from "react";
import { Apps } from "./Apps";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Apps",
  component: Apps,
};

export const Default = () => <Apps />;
export const Fill = () => <Apps fill="blue" />;
export const Size = () => <Apps height="50" width="50" />;
export const CustomStyle = () => <Apps style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Apps className="custom-class" />;
export const Clickable = () => <Apps onClick={()=>console.log("clicked")} />;

const Template = (args) => <Apps {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
