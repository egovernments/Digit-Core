import React from "react";
import { PregnantWoman } from "./PregnantWoman";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "PregnantWoman",
  component: PregnantWoman,
};

export const Default = () => <PregnantWoman />;
export const Fill = () => <PregnantWoman fill="blue" />;
export const Size = () => <PregnantWoman height="50" width="50" />;
export const CustomStyle = () => <PregnantWoman style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <PregnantWoman className="custom-class" />;

export const Clickable = () => <PregnantWoman onClick={()=>console.log("clicked")} />;

const Template = (args) => <PregnantWoman {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
