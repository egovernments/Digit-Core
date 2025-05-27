import React from "react";
import { SixFtApart } from "./SixFtApart";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "SixFtApart",
  component: SixFtApart,
};

export const Default = () => <SixFtApart />;
export const Fill = () => <SixFtApart fill="blue" />;
export const Size = () => <SixFtApart height="50" width="50" />;
export const CustomStyle = () => <SixFtApart style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SixFtApart className="custom-class" />;

export const Clickable = () => <SixFtApart onClick={()=>console.log("clicked")} />;

const Template = (args) => <SixFtApart {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
