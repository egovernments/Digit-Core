import React from "react";
import { LocalCarWash } from "./LocalCarWash";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "LocalCarWash",
  component: LocalCarWash,
};

export const Default = () => <LocalCarWash />;
export const Fill = () => <LocalCarWash fill="blue" />;
export const Size = () => <LocalCarWash height="50" width="50" />;
export const CustomStyle = () => <LocalCarWash style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <LocalCarWash className="custom-class" />;

export const Clickable = () => <LocalCarWash onClick={()=>console.log("clicked")} />;

const Template = (args) => <LocalCarWash {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
