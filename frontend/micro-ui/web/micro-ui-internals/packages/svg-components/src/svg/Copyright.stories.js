import React from "react";
import { Copyright } from "./Copyright";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Copyright",
  component: Copyright,
};

export const Default = () => <Copyright />;
export const Fill = () => <Copyright fill="blue" />;
export const Size = () => <Copyright height="50" width="50" />;
export const CustomStyle = () => <Copyright style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Copyright className="custom-class" />;

export const Clickable = () => <Copyright onClick={()=>console.log("clicked")} />;

const Template = (args) => <Copyright {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
