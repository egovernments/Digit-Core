import React from "react";
import { UnfoldLess } from "./UnfoldLess";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "UnfoldLess",
  component: UnfoldLess,
};

export const Default = () => <UnfoldLess />;
export const Fill = () => <UnfoldLess fill="blue" />;
export const Size = () => <UnfoldLess height="50" width="50" />;
export const CustomStyle = () => <UnfoldLess style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <UnfoldLess className="custom-class" />;
export const Clickable = () => <UnfoldLess onClick={()=>console.log("clicked")} />;

const Template = (args) => <UnfoldLess {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
