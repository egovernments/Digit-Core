import React from "react";
import { ThumbDownOffAlt } from "./ThumbDownOffAlt";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ThumbDownOffAlt",
  component: ThumbDownOffAlt,
};

export const Default = () => <ThumbDownOffAlt />;
export const Fill = () => <ThumbDownOffAlt fill="blue" />;
export const Size = () => <ThumbDownOffAlt height="50" width="50" />;
export const CustomStyle = () => <ThumbDownOffAlt style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ThumbDownOffAlt className="custom-class" />;
export const Clickable = () => <ThumbDownOffAlt onClick={()=>console.log("clicked")} />;

const Template = (args) => <ThumbDownOffAlt {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
