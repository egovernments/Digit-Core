import React from "react";
import { Preview } from "./Preview";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Preview",
  component: Preview,
};

export const Default = () => <Preview />;
export const Fill = () => <Preview fill="blue" />;
export const Size = () => <Preview height="50" width="50" />;
export const CustomStyle = () => <Preview style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Preview className="custom-class" />;

export const Clickable = () => <Preview onClick={()=>console.log("clicked")} />;

const Template = (args) => <Preview {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
