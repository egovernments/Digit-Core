import React from "react";
import { Update } from "./Update";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Update",
  component: Update,
};

export const Default = () => <Update />;
export const Fill = () => <Update fill="blue" />;
export const Size = () => <Update height="50" width="50" />;
export const CustomStyle = () => <Update style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Update className="custom-class" />;
export const Clickable = () => <Update onClick={()=>console.log("clicked")} />;

const Template = (args) => <Update {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};

