import React from "react";
import { Description } from "./Description";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Description",
  component: Description,
};

export const Default = () => <Description />;
export const Fill = () => <Description fill="blue" />;
export const Size = () => <Description height="50" width="50" />;
export const CustomStyle = () => <Description style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Description className="custom-class" />;

export const Clickable = () => <Description onClick={()=>console.log("clicked")} />;

const Template = (args) => <Description {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
