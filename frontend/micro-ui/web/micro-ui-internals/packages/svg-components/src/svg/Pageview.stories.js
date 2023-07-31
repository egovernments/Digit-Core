import React from "react";
import { Pageview } from "./Pageview";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Pageview",
  component: Pageview,
};

export const Default = () => <Pageview />;
export const Fill = () => <Pageview fill="blue" />;
export const Size = () => <Pageview height="50" width="50" />;
export const CustomStyle = () => <Pageview style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Pageview className="custom-class" />;

export const Clickable = () => <Pageview onClick={()=>console.log("clicked")} />;

const Template = (args) => <Pageview {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
