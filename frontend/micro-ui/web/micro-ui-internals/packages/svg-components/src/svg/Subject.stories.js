import React from "react";
import { Subject } from "./Subject";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Subject",
  component: Subject,
};

export const Default = () => <Subject />;
export const Fill = () => <Subject fill="blue" />;
export const Size = () => <Subject height="50" width="50" />;
export const CustomStyle = () => <Subject style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Subject className="custom-class" />;

export const Clickable = () => <Subject onClick={()=>console.log("clicked")} />;

const Template = (args) => <Subject {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
