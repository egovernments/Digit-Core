import React from "react";
import { Share } from "./Share";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Share",
  component: Share,
};

export const Default = () => <Share />;
export const Fill = () => <Share fill="blue" />;
export const Size = () => <Share height="50" width="50" />;
export const CustomStyle = () => <Share style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Share className="custom-class" />;

export const Clickable = () => <Share onClick={()=>console.log("clicked")} />;

const Template = (args) => <Share {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
